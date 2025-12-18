import {
  Component,
  ViewChild,
  type ElementRef,
  type OnInit,
  type AfterViewInit,
  type OnDestroy,
   NgZone,
  HostListener,
} from "@angular/core"
import { PickerModule } from "@ctrl/ngx-emoji-mart"
import { FormBuilder, type FormGroup, ReactiveFormsModule, Validators } from "@angular/forms"
import { CommonModule } from "@angular/common"
import { HttpClient } from "@angular/common/http"
import { NgbModal, NgbModule } from "@ng-bootstrap/ng-bootstrap"
import { Client, type IMessage, type StompSubscription } from "@stomp/stompjs"
import type { ChatMessage, loadMessages } from "./chat.model"
import type { FrontendUser, UserSearchResponse } from "./usersearchresponse.model"
import SockJS from "sockjs-client"
import { SimplebarAngularModule } from "simplebar-angular"

@Component({
  selector: "app-chat",
  standalone: true,
  imports: [ReactiveFormsModule, NgbModule, CommonModule, PickerModule, SimplebarAngularModule],
  providers: [FormBuilder],
  templateUrl: "./chat.component.html",
  styleUrls: ["./chat.component.scss"],
})
export class ChatComponent implements OnInit, AfterViewInit, OnDestroy {
  @ViewChild("scrollRef") scrollRef!: ElementRef<any>
  @ViewChild("createGroupModal") createGroupModal!: ElementRef
  @ViewChild("addMemberModal") addMemberModal!: ElementRef

  username = "Loading..."
  isActive = false
  selectedGroup: { id: string; name: string; chatMemberId?: string } | null = null
  userId = ""
  isAdmin = false
  newGroupNotification: { id: string; name: string } | null = null

  breadCrumbItems = [{ label: "Skote" }, { label: "Chat", active: true }]
  chatMessagesData: ChatMessage[] = []
  loadGroupMessages: loadMessages[] = []
  groups: { id: string; name: string; chatMemberId?: string }[] = []
  searchedUsers: FrontendUser[] = []
  selectedGroupMembers: { id: string; userId: string; username: string; role: string }[] = []

  formData!: FormGroup
  groupForm!: FormGroup
  chatSubmit = false

  private stompClient: Client | null = null
  private messageSubscription: StompSubscription | null = null
  private presenceSubscription: StompSubscription | null = null
  private onlineUsersSubscription: StompSubscription | null = null

  // File handling
  selectedFile: File | null = null
  selectedFileType: "IMAGE" | "FILE" | "OTHER" | null = null
  previewImage: string | null = null

  showEmojiPicker = false

  groupUserSearchResults: FrontendUser[] = []
  selectedUsersForGroup: FrontendUser[] = []

  addMemberSearchResults: FrontendUser[] = []
  selectedUsersForAddMember: FrontendUser[] = []
  allUsers: FrontendUser[] = []

  userPresence: Map<string, { isOnline: boolean; lastSeen?: string; username?: string; profilePictureUrl?: string }> =
    new Map()

  constructor(
    private fb: FormBuilder,
    private http: HttpClient,
    public modalService: NgbModal,
    private ngZone: NgZone,
  ) {}

  ngOnInit() {
    if (typeof window !== "undefined") this.userId = localStorage.getItem("userId") || ""
    if (!this.userId) return console.error("No userId found! WebSocket and group loading will not work.")

    this.formData = this.fb.group({ message: ["", Validators.required] })
    this.groupForm = this.fb.group({ groupName: ["", Validators.required] })

    this.fetchGroups()
    this.setupWebSocket()
    this.loadCurrentUser()
    this.loadAllUsers()
  }

  ngAfterViewInit() {
    this.scrollToBottom()
  }

  ngOnDestroy() {
    this.messageSubscription?.unsubscribe()
    this.presenceSubscription?.unsubscribe()
    this.onlineUsersSubscription?.unsubscribe()
    this.stompClient?.deactivate()
  }

  get form() {
    return this.formData.controls
  }

  get groupFormControls() {
    return this.groupForm.controls
  }

  loadCurrentUser() {
    if (!this.userId) return
    this.http.get<any>(`http://localhost:8080/api/users/${this.userId}`).subscribe({
      next: (user) => {
        this.username = user.username
        this.isActive = true
      },
      error: (err) => console.error("Error loading user:", err),
    })
  }

  fetchGroups() {
    this.http
      .get<{ id: string; name: string }[]>("http://localhost:8080/api/group-chats", { params: { userId: this.userId } })
      .subscribe({
        next: (data) => {
          this.groups = data.map((g) => ({ ...g, chatMemberId: undefined }))
          this.groups.forEach((g) => this.loadGroupMemberId(g))
        },
        error: (err) => console.error("Error fetching groups:", err),
      })
  }

  loadGroupMemberId(group: {
    id: string
    name: string
    chatMemberId?: string
  }) {
    this.http
      .get<{ id: string; userId: string; username: string; role: string }[]>(
        `http://localhost:8080/api/group-chats/${group.id}/members`,
      )
      .subscribe({
        next: (members) => {
          const me = members.find((m) => m.userId === this.userId)
          if (me) group.chatMemberId = me.id
        },
        error: (err) => console.error(`Error loading members for group ${group.id}:`, err),
      })
  }

  loadGroupMembers(chatId: string) {
    this.http
      .get<{ id: string; userId: string; username: string; role: string }[]>(
        `http://localhost:8080/api/group-chats/${chatId}/members`,
      )
      .subscribe({
        next: (members) => {
          this.selectedGroupMembers = members
          const me = members.find((m) => m.userId === this.userId)
          if (me && this.selectedGroup) this.selectedGroup.chatMemberId = me.id
        },
        error: (err) => console.error("Error loading group members:", err),
      })
  }

  selectGroup(group: { id: string; name: string; chatMemberId?: string }) {
    this.selectedGroup = group
    this.chatMessagesData = []
    this.fetchGroupMessages(group.id)
    this.checkAdminStatus(group.id)
    this.subscribeToGroup(group.id)
  }

  fetchGroupMessages(chatId: string) {
    this.http.get<loadMessages[]>(`http://localhost:8080/api/group-chats/${chatId}/messages`).subscribe({
      next: (messages) => (this.loadGroupMessages = messages),
      error: (err) => console.error("Error fetching group messages", err),
    })
  }

  checkAdminStatus(groupId: string) {
    this.http
      .get<{ id: string; userId: string; username: string; role: string }[]>(
        `http://localhost:8080/api/group-chats/${groupId}/members`,
      )
      .subscribe({
        next: (members) => {
          const member = members.find((m) => m.userId === this.userId)
          this.isAdmin = member?.role === "ADMIN"
          if (member && this.selectedGroup) this.selectedGroup.chatMemberId = member.id
        },
        error: (err) => console.error("Error checking admin status:", err),
      })
  }

  messageSave() {
    if (!this.selectedGroup || (!this.formData.valid && !this.selectedFile)) return
    const chatMemberId = this.selectedGroup.chatMemberId
    if (!chatMemberId) return console.error("No chatMemberId for this group")

    const backendUrl = "http://localhost:8080"

    if (this.selectedFile) {
      const formData = new FormData()
      formData.append("file", this.selectedFile)
      formData.append("userId", this.userId)
      formData.append("messageType", this.selectedFileType === "OTHER" ? "FILE" : this.selectedFileType!)
      const chatId = this.selectedGroup.id
      this.http.post(`${backendUrl}/api/group-chats/${chatId}/messages/upload`, formData).subscribe({
        next: () => {
          this.selectedFile = null
          this.selectedFileType = null
          this.previewImage = null
          this.formData.reset()
          this.scrollToBottom()
        },
        error: (err) => console.error("File upload failed:", err),
      })
      return
    }

    const request = {
      chatId: this.selectedGroup.id,
      chatMemberId,
      content: this.formData.get("message")?.value,
      messageType: "TEXT",
      userId: this.userId,
    }

    if (this.stompClient?.active) {
      this.stompClient.publish({
        destination: "/app/sendGroupMessage",
        body: JSON.stringify(request),
      })
    }

    this.formData.reset()
    this.chatSubmit = true
    this.scrollToBottom()
  }

  pickFile(event: Event, type: "IMAGE" | "FILE") {
    const input = event.target as HTMLInputElement
    if (!input.files?.length) return
    this.selectedFile = input.files[0]
    this.selectedFileType = type
    if (type === "IMAGE") {
      const reader = new FileReader()
      reader.onload = () => (this.previewImage = reader.result as string)
      reader.readAsDataURL(this.selectedFile)
    }
  }

  setupWebSocket() {
    if (!this.userId) return
    const socketUrl = `http://localhost:8080/ws?userId=${this.userId}`
    this.stompClient = new Client({
      webSocketFactory: () => new SockJS(socketUrl),
      debug: (str) => console.log(str),
      connectHeaders: {
        userId: this.userId,
      },
    })

    this.stompClient.onConnect = () => {
      console.log("WebSocket connected")

      this.setupPresenceSubscriptions()

      this.stompClient?.subscribe(`/user/queue/groups`, (message: IMessage) => {
        console.log("New group message received:", message.body)
        const newGroup = JSON.parse(message.body)

        const groupId = newGroup.id || newGroup.chatId
        if (!groupId) return

        if (!this.groups.find((g) => g.id === groupId)) {
          const groupObj = {
            id: groupId,
            name: newGroup.name,
            chatMemberId: undefined,
          }

          this.ngZone.run(() => {
            this.groups.push(groupObj)
            this.loadGroupMemberId(groupObj)
            this.newGroupNotification = { id: groupId, name: newGroup.name }
            setTimeout(() => (this.newGroupNotification = null), 5000)
          })
        }
      })
    }

    this.stompClient.onStompError = (frame) => console.error("Broker error:", frame.headers["message"])
    this.stompClient.onWebSocketClose = () => {
      console.warn("WebSocket closed. Reconnecting in 5 seconds...")
      setTimeout(() => this.stompClient?.activate(), 5000)
    }

    this.stompClient.activate()
  }

  setupPresenceSubscriptions() {
    if (!this.stompClient) return

    // Subscribe to individual user status changes
    this.presenceSubscription = this.stompClient.subscribe("/topic/status", (message: IMessage) => {
      try {
        const statusUpdate = JSON.parse(message.body)
        this.ngZone.run(() => {
          this.userPresence.set(statusUpdate.userId, {
            isOnline: statusUpdate.online,
            lastSeen: statusUpdate.lastSeen,
            username: statusUpdate.username,
            profilePictureUrl: statusUpdate.profilePictureUrl,
          })
        })
      } catch (err) {
        console.error("Error parsing status update:", err)
      }
    })

    // Subscribe to full online users list
    this.onlineUsersSubscription = this.stompClient.subscribe("/topic/online-users", (message: IMessage) => {
      try {
        const onlineUsers = JSON.parse(message.body)
        this.ngZone.run(() => {
          // Clear current presence and update with new data
          this.userPresence.clear()
          Object.entries(onlineUsers).forEach(([userId, status]: [string, any]) => {
            this.userPresence.set(userId, {
              isOnline: status.online,
              lastSeen: status.lastSeen,
              username: status.username,
              profilePictureUrl: status.profilePictureUrl,
            })
          })
        })
      } catch (err) {
        console.error("Error parsing online users:", err)
      }
    })

    // Load initial presence data
    this.loadInitialPresence()
  }

  loadInitialPresence() {
    this.http.get<any>("http://localhost:8080/api/presence").subscribe({
      next: (presenceData) => {
        this.ngZone.run(() => {
          this.userPresence.clear()
          Object.entries(presenceData).forEach(([userId, presence]: [string, any]) => {
            this.userPresence.set(userId, {
              isOnline: presence.online,
              lastSeen: presence.lastSeen,
              username: presence.user?.username,
              profilePictureUrl: presence.user?.profilePictureUrl,
            })
          })
        })
      },
      error: (err) => console.error("Error loading initial presence:", err),
    })
  }

  isUserOnline(userId: string): boolean {
    return this.userPresence.get(userId)?.isOnline || false
  }

  getUserStatusClass(userId: string): string {
    return this.isUserOnline(userId) ? "user-status-online" : "user-status-offline"
  }

  subscribeToGroup(groupId: string) {
    if (this.messageSubscription) {
      this.messageSubscription.unsubscribe()
      this.messageSubscription = null
    }
    if (!this.stompClient) return

    const subscribeFn = () => {
      this.messageSubscription =
        this.stompClient?.subscribe(`/topic/chat/${groupId}`, (message: IMessage) => {
          try {
            const newMessage: ChatMessage = JSON.parse(message.body)
            if (!newMessage.messageType) return
            newMessage.align = newMessage.userId === this.userId ? "right" : "left"
            if (!newMessage.chatMember)
              newMessage.chatMember = {
                id: newMessage.chatMemberId,
                user: {
                  id: newMessage.userId,
                  name: newMessage.name || "Unknown",
                },
              }
            this.chatMessagesData.push(newMessage)
            this.scrollToBottom()
          } catch (err) {
            console.error("Error parsing incoming message", err)
          }
        }) ?? null
    }

    if (this.stompClient.active) subscribeFn()
    else {
      const previousOnConnect = this.stompClient.onConnect
      this.stompClient.onConnect = (frame) => {
        if (previousOnConnect) previousOnConnect(frame)
        subscribeFn()
      }
    }
  }

  searchUsersForGroup(event: Event) {
    const query = (event.target as HTMLInputElement).value
    if (!query) {
      this.groupUserSearchResults = []
      return
    }
    this.http
      .get<{ data: UserSearchResponse[] }>("http://localhost:8080/api/users/search", {
        params: { query, limit: "10", userId: this.userId },
      })
      .subscribe({
        next: (res) => {
          this.groupUserSearchResults = res.data.map((u) => ({
            id: u.id,
            name: u.username,
            image: u.profilePictureUrl || null,
          }))
        },
        error: (err) => console.error("Error searching users for group:", err),
      })
  }

  toggleUserSelection(user: FrontendUser) {
    const index = this.selectedUsersForGroup.findIndex((u) => u.id === user.id)
    if (index > -1) {
      this.selectedUsersForGroup.splice(index, 1)
    } else {
      this.selectedUsersForGroup.push(user)
    }
  }

  isUserSelected(userId: string): boolean {
    return this.selectedUsersForGroup.some((u) => u.id === userId)
  }

  removeUserSelection(userId: string) {
    this.selectedUsersForGroup = this.selectedUsersForGroup.filter((u) => u.id !== userId)
  }

  createGroup() {
  if (!this.groupForm.valid) return;

  const request = {
    groupName: this.groupForm.get("groupName")?.value,
    createdById: this.userId,
    members: this.selectedUsersForGroup.map((u) => u.id), // ✅ FIXED (was memberIds)
  };

  this.http
    .post<{ id: string; name: string }>("http://localhost:8080/api/group-chats", request)
    .subscribe({
      next: (group) => {
        if (group && !this.groups.find((g) => g.id === group.id)) {
          this.ngZone.run(() => {
            this.groups.push({
              id: group.id,
              name: group.name,
              chatMemberId: undefined,
            });
            this.loadGroupMemberId(group);
          });
        }

        this.groupForm.reset();
        this.selectedUsersForGroup = [];
        this.groupUserSearchResults = [];
        this.modalService.dismissAll();
      },
      error: (err) => console.error("Error creating group:", err),
    });
}


  searchUsers(event: Event) {
    const query = (event.target as HTMLInputElement).value
    if (!query) {
      this.searchedUsers = []
      return
    }
    this.http
      .get<{ data: UserSearchResponse[] }>("http://localhost:8080/api/users/search", {
        params: { query, limit: "10", userId: this.userId },
      })
      .subscribe({
        next: (res) => {
          this.searchedUsers = res.data.map((u) => ({
            id: u.id,
            name: u.username,
            image: u.profilePictureUrl || null,
          }))
        },
        error: (err) => console.error("Error searching users:", err),
      })
  }

  addMember(userId: string) {
    if (!this.selectedGroup?.id || !this.isAdmin) return
    this.http.post(`http://localhost:8080/api/group-chats/${this.selectedGroup.id}/members`, { userId }).subscribe({
      next: () => {
        if (userId === this.userId && this.selectedGroup && !this.groups.find((g) => g.id === this.selectedGroup?.id)) {
          this.groups.push({
            id: this.selectedGroup.id,
            name: this.selectedGroup.name,
            chatMemberId: undefined,
          })
          this.loadGroupMemberId(this.selectedGroup)
        }
      },
      error: (err) => console.error("Error adding member:", err),
    })
  }

  removeMember(userId: string) {
    if (!this.selectedGroup?.id || !this.isAdmin) return
    this.http.delete(`http://localhost:8080/api/group-chats/${this.selectedGroup.id}/members/${userId}`).subscribe({
      next: () => {},
      error: (err) => console.error("Error removing member:", err),
    })
  }

  toggleEmojiPicker() {
    this.showEmojiPicker = !this.showEmojiPicker
  }

  addEmoji(event: any) {
    const emoji = event?.emoji?.native || ""
    const currentMessage = this.formData.get("message")?.value || ""
    this.formData.get("message")?.setValue(currentMessage + emoji)
  }

  @HostListener("document:click", ["$event"])
  clickOutside(event: Event) {
    const picker = document.querySelector(".emoji-picker-container")
    const button = (event.target as HTMLElement).closest(".bx-smile")
    if (picker && !picker.contains(event.target as Node) && !button) {
      this.showEmojiPicker = false
    }
  }

  scrollToBottom() {
    if (typeof window === "undefined") return
    if (!this.scrollRef?.nativeElement) return
    setTimeout(() => {
      this.scrollRef.nativeElement.scrollTop = this.scrollRef.nativeElement.scrollHeight
    }, 0)
  }

  getGroupColor(groupName: string): string {
    // Template-inspired color palette matching Skote design
    const colors = [
      "#556ee6", // Primary blue
      "#34c38f", // Success green
      "#f1b44c", // Warning yellow
      "#f46a6a", // Danger red
      "#50a5f1", // Info cyan
      "#564ab1", // Indigo
      "#f1734f", // Orange
      "#e83e8c", // Pink
    ]

    // Generate consistent color based on group name
    let hash = 0
    for (let i = 0; i < groupName.length; i++) {
      hash = groupName.charCodeAt(i) + ((hash << 5) - hash)
    }

    return colors[Math.abs(hash) % colors.length]
  }

  loadAllUsers() {
    this.http.get<{ data: FrontendUser[] }>("http://localhost:8080/api/users").subscribe({
      next: (response) => {
        this.allUsers = response.data || (response as any) || []
        // Map the response to match FrontendUser interface
        this.allUsers = this.allUsers.map((user: any) => ({
          id: user.id,
          name: user.username || user.name,
          image: user.profilePictureUrl || user.image || null,
        }))
      },
      error: (err) => console.error("Error loading all users:", err),
    })
  }

  searchUsersForAddMember(event: Event) {
    const query = (event.target as HTMLInputElement).value
    if (!query) {
      this.addMemberSearchResults = []
      return
    }
    this.http
      .get<{ data: UserSearchResponse[] }>("http://localhost:8080/api/users/search", {
        params: { query, limit: "10", userId: this.userId },
      })
      .subscribe({
        next: (res) => {
          this.addMemberSearchResults = res.data.map((u) => ({
            id: u.id,
            name: u.username,
            image: u.profilePictureUrl || null,
          }))
        },
        error: (err) => console.error("Error searching users for add member:", err),
      })
  }

  toggleAddMemberSelection(user: FrontendUser) {
    const index = this.selectedUsersForAddMember.findIndex((u) => u.id === user.id)
    if (index > -1) {
      this.selectedUsersForAddMember.splice(index, 1)
    } else {
      this.selectedUsersForAddMember.push(user)
    }
  }

  isAddMemberSelected(userId: string): boolean {
    return this.selectedUsersForAddMember.some((u) => u.id === userId)
  }

  removeAddMemberSelection(userId: string) {
    this.selectedUsersForAddMember = this.selectedUsersForAddMember.filter((u) => u.id !== userId)
  }

  addSelectedMembers() {
    if (!this.selectedGroup?.id || !this.isAdmin || this.selectedUsersForAddMember.length === 0) return

    // Add each selected user to the group
    const addPromises = this.selectedUsersForAddMember.map((user) =>
      this.http
        .post(`http://localhost:8080/api/group-chats/${this.selectedGroup!.id}/members`, { userId: user.id })
        .toPromise(),
    )

    Promise.all(addPromises)
      .then(() => {
        console.log("All members added successfully")
        // Reset the selection
        this.selectedUsersForAddMember = []
        this.addMemberSearchResults = []
        // Reload group members if needed
        this.loadGroupMembers(this.selectedGroup!.id)
      })
      .catch((err) => {
        console.error("Error adding some members:", err)
      })
  }
}
