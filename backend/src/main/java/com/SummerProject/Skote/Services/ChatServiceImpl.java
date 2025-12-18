package com.SummerProject.Skote.Services;

import com.SummerProject.Skote.Controllers.ChatController;
import com.SummerProject.Skote.DTO.*;
import com.SummerProject.Skote.Repositories.ChatMemberRepo;
import com.SummerProject.Skote.Repositories.ChatRepo;
import com.SummerProject.Skote.Repositories.MessageRepo;
import com.SummerProject.Skote.Repositories.UserRepo;
import com.SummerProject.Skote.abstracts.ChatMemberService;
import com.SummerProject.Skote.abstracts.ChatService;
import com.SummerProject.Skote.models.Chat;
import com.SummerProject.Skote.models.ChatMember;
import com.SummerProject.Skote.models.Message;
import com.SummerProject.Skote.shared.CustomeResponseException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl{

    private ChatMemberService chatMemberService;
    private final ChatRepo chatRepo;
    private final ChatMemberRepo chatMemberRepo;
    private final MessageRepo messageRepo;
    private final UserRepo userRepo;
    @Getter
    private final SimpMessagingTemplate simpMessagingTemplate;

    // ---------------- Group Chat ----------------

    public Chat createGroupChat(GroupChatRequest request) {
        UUID createdById = request.getCreatedById();
        var user = userRepo.findById(createdById)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("Invalid userId: " + createdById));

        Chat chat = new Chat();
        chat.setName(request.getGroupName());
        chat.setIsGroup(true);
        chat.setCreatedAt(LocalDateTime.now());
        chat.setCreatedBy(user);
        chat = chatRepo.save(chat);

        // Add creator as ADMIN
        ChatMember creatorMember = new ChatMember();
        creatorMember.setChat(chat);
        creatorMember.setUser(user);
        creatorMember.setRole(Role.ADMIN);
        chatMemberRepo.save(creatorMember);
        if(!request.getMembers().isEmpty()){
            Chat finalChat = chat;
            request.getMembers().forEach(member ->{
                chatMemberService.addMemberToGroup(member, finalChat.getId());
            });

        }


        // 🔥 Notify creator immediately (per-user destination)
        simpMessagingTemplate.convertAndSendToUser(
                user.getId().toString(),          // principal name (your handshake sets this)
                "/queue/groups",                  // user queue destination (no userId embedded)
                new SimpleChatDTO(chat.getId(), chat.getName(), true)
        );


        return chat;
    }

    // ---------------- Send Message ----------------

    public ChatMessageDTO sendGroupMessage(MessageRequest request) {
        Chat chat = chatRepo.findById(request.getChatId())
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("Chat not found with id " + request.getChatId()));

        UUID userUuid = UUID.fromString(request.getUserId());

        ChatMember chatMember = chatMemberRepo.findByChatIdAndUserId(request.getChatId(), userUuid)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("User is not a member of this chat"));

        Message message = new Message();
        message.setChatMember(chatMember);
        message.setContent(request.getContent());
        message.setMessageType(request.getMessageType());
        message.setCreatedAt(LocalDateTime.now());
        message.setIsRead(false);
        message.setIsDeleted(false);
        message.setIsEdited(false);

        Message savedMessage = messageRepo.save(message);

        ChatMessageDTO dto = mapToChatMessageDTO(savedMessage);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), dto);

        return dto;
    }


    public ChatMessageDTO sendFileMessage(UUID chatId, MultipartFile file, String userId, MessageType messageType) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("Chat not found"));

        UUID userUuid = UUID.fromString(userId);

        ChatMember chatMember = chatMemberRepo.findByChatIdAndUserId(chatId, userUuid)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("User is not a member of this chat"));

        try {
            // Generate unique file name
            String originalFilename = file.getOriginalFilename();
            String extension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String filename = UUID.randomUUID() + extension;

            // Save file to uploads folder
            Path uploadPath = Paths.get("uploads").toAbsolutePath();
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            file.transferTo(filePath);

            // Build file URL
            String fileUrl = "/uploads/" + filename;

            // Create message
            Message message = new Message();
            message.setChatMember(chatMember);
            message.setContent(originalFilename != null ? originalFilename : "");
            message.setMessageType(messageType);
            message.setFileUrl(fileUrl);
            message.setFileName(originalFilename);
            message.setFileType(file.getContentType());
            message.setCreatedAt(LocalDateTime.now());
            message.setIsRead(false);
            message.setIsEdited(false);
            message.setIsDeleted(false);

            Message savedMessage = messageRepo.save(message);

            // Map to DTO using the updated helper
            ChatMessageDTO dto = mapToChatMessageDTO(savedMessage);

            // Broadcast via WebSocket
            simpMessagingTemplate.convertAndSend("/topic/chat/" + chat.getId(), dto);

            return dto;

        } catch (Exception e) {
            throw CustomeResponseException.invalidCredentials("Failed to upload file: " + e.getMessage());
        }
    }


    // ---------------- Get Group Messages ----------------

    public List<LoadMessageDTO> getGroupMessages(UUID chatId) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("No chat found with id " + chatId));

        return messageRepo.findByChatMember_ChatIdOrderByCreatedAtAsc(chat.getId())
                .stream()
                .map(this::mapToLoadMessageDTO)
                .collect(Collectors.toList());
    }

    private LoadMessageDTO mapToLoadMessageDTO(Message message) {
        return new LoadMessageDTO(
                message.getId(),
                message.getContent(),
                message.getChatMember().getUser().getId(),
                message.getChatMember().getUser().getUsername(),
                message.getCreatedAt(),
                message.getMessageType(),
                message.getFileUrl()
        );
    }


    // ---------------- Get Chat Members ----------------

    public List<ChatMember> findByChatId(UUID chatId) {
        Chat chat = chatRepo.findById(chatId)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("No chat found with id " + chatId));
        return chatMemberRepo.findByChatId(chat.getId());
    }




    // ---------------- Modify Message ----------------
    public ChatMessageDTO modifyMessage(ModifyMessageRequest request) {
        Message message = messageRepo.findById(request.getMessageId())
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("Message not found"));

        if (!message.getChatMember().getChat().getId().equals(request.getChatId()))
            throw CustomeResponseException.badRequest("Message does not belong to this chat");

        if (!message.getChatMember().getUser().getId().equals(request.getUserId()))
            throw CustomeResponseException.badRequest("Only the sender can modify this message");

        message.setContent(request.getNewContent());
        message.setUpdatedAt(LocalDateTime.now());
        message.setIsEdited(true);
        messageRepo.save(message);

        ChatMessageDTO dto = mapToChatMessageDTO(message);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + request.getChatId(), dto);
        return dto;
    }

    // ---------------- Delete Message ----------------
    public ChatMessageDTO deleteMessage(UUID chatId, UUID messageId, UUID userId) {
        Message message = messageRepo.findById(messageId)
                .orElseThrow(() -> CustomeResponseException.resourceNotFound("Message not found"));

        if (!message.getChatMember().getChat().getId().equals(chatId))
            throw CustomeResponseException.badRequest("Message does not belong to this chat");

        if (!message.getChatMember().getUser().getId().equals(userId))
            throw CustomeResponseException.badRequest("Only the sender can delete this message");

        message.setIsDeleted(true);
        messageRepo.save(message);

        ChatMessageDTO dto = mapToChatMessageDTO(message);
        simpMessagingTemplate.convertAndSend("/topic/chat/" + chatId, dto);
        return dto;
    }

    // ---------------- Helper: Map Message to DTO ----------------
    private ChatMessageDTO mapToChatMessageDTO(Message message) {
        return new ChatMessageDTO(
                message.getId(),
                message.getChatMember().getChat().getId(),
                message.getChatMember().getId(),
                message.getContent(),
                message.getContent(), // optional UI binding
                message.getMessageType(),
                message.getCreatedAt(),
                message.getUpdatedAt(),
                message.getIsRead(),
                message.getIsEdited(),
                message.getIsDeleted(),
                message.getChatMember().getUser().getId(),
                message.getChatMember().getUser().getUsername(),
                message.getFileUrl(),   // null if not a file/image
                message.getFileName(),  // null if not a file/image
                message.getFileType()   // null if not a file/image
        );
    }

}
