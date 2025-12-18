package com.SummerProject.Skote.Controllers;

import com.SummerProject.Skote.DTO.*;
import com.SummerProject.Skote.Services.ChatMemberServiceImpl;
import com.SummerProject.Skote.Services.ChatServiceImpl;
import com.SummerProject.Skote.models.Chat;
import com.SummerProject.Skote.models.ChatMember;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api")
public class ChatController {

    @Autowired
    private ChatServiceImpl chatService;

    @Autowired
    private ChatMemberServiceImpl chatMemberService;

    @PostMapping("/group-chats")
    public Chat createGroupChat(@RequestBody GroupChatRequest request) {
        return chatService.createGroupChat(request);
    }

    @PostMapping("/group-chats/{chatId}/members")
    public AddMemberResponse addMember(@PathVariable UUID chatId, @RequestBody AddMemberRequest request) {
        ChatMember chatmember = chatMemberService.addMemberToGroup(chatId, request.getUserId());
        AddMemberResponse member = new AddMemberResponse();
        member.setChatId(chatmember.getChat().getId());
        member.setUserId(chatmember.getUser().getId());
        member.setRole(Role.MEMBER);

        return member;
    }

    @DeleteMapping("/group-chats/{chatId}/members/{userId}")
    public void removeMember(@PathVariable UUID chatId, @PathVariable UUID userId) {
        chatMemberService.removeMemberFromGroup(chatId, userId);
    }

    @GetMapping("/group-chats/{chatId}/messages")
    public ResponseEntity<List<LoadMessageDTO>> getGroupMessages(@PathVariable UUID chatId) {
        List<LoadMessageDTO> messages = chatService.getGroupMessages(chatId);
        return ResponseEntity.ok(messages);
    }


    // === FIXED ===
    @GetMapping("/group-chats")
    public List<SimpleChat> getUserGroups(@RequestParam UUID userId) {
        // fetch groups for this user
        List<ChatMember> members = chatMemberService.findByUserId(userId);
        return members.stream()
                .map(ChatMember::getChat)
                .filter(Objects::nonNull)
                .map(chat -> new SimpleChat(chat.getId(), chat.getName()))
                .collect(Collectors.toList());
    }

    @GetMapping("/group-chats/{chatId}/members")
    public List<ChatMemberResponse> getGroupMembers(@PathVariable UUID chatId) {
        return chatMemberService.findByChatId(chatId).stream()
                .map(m -> new ChatMemberResponse(
                        m.getId(),
                        m.getUser().getId(),
                        m.getUser().getUsername(),
                        m.getRole()
                ))
                .collect(Collectors.toList());
    }

    @PostMapping("/group-chats/{chatId}/messages/upload")
    public ResponseEntity<ChatMessageDTO> uploadFileMessage(
            @PathVariable UUID chatId,
            @RequestParam("file") MultipartFile file,
            @RequestParam("userId") String userId,
            @RequestParam("messageType") MessageType messageType
    ) {
        ChatMessageDTO messageDto = chatService.sendFileMessage(chatId, file, userId, messageType);
        return ResponseEntity.ok(messageDto);
    }


    // DTO to return only id and name to frontend
    public record SimpleChat(UUID id, String name) {}
}
