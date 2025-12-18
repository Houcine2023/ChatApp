package com.SummerProject.Skote.Services;

import com.SummerProject.Skote.DTO.ChatMemberResponse;
import com.SummerProject.Skote.DTO.Role;
import com.SummerProject.Skote.DTO.SimpleChatDTO;
import com.SummerProject.Skote.Repositories.ChatMemberRepo;
import com.SummerProject.Skote.Repositories.ChatRepo;
import com.SummerProject.Skote.Repositories.UserRepo;
import com.SummerProject.Skote.abstracts.ChatMemberService;
import com.SummerProject.Skote.models.ChatMember;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class ChatMemberServiceImpl implements ChatMemberService {

    @Autowired
    private ChatMemberRepo chatMemberRepo;
    @Autowired
    private ChatRepo chatRepo;
    @Autowired
    private UserRepo userRepo;
    @Autowired
    private SimpMessagingTemplate simpMessagingTemplate;


    @Override
    public ChatMember getChatMember(UUID uuid) {
        return null;
    }

    @Override
    public ChatMember getChatMember(String name) {
        return null;
    }

    @Override
    public ChatMember addMemberToGroup(UUID chatId, UUID userId) {
        if (chatMemberRepo.existsByChatIdAndUserId(chatId, userId)) {
            throw new RuntimeException("User already in group");
        }

        ChatMember newMember = new ChatMember();
        newMember.setChat(chatRepo.findById(chatId).orElseThrow());
        newMember.setUser(userRepo.findById(userId).orElseThrow());
        newMember.setRole(Role.MEMBER);
        ChatMember savedMember = chatMemberRepo.save(newMember);

        // Broadcast the new member to everyone in this group
        simpMessagingTemplate.convertAndSend(
                "/topic/group/" + chatId + "/members",
                new ChatMemberResponse(
                        savedMember.getId(),
                        savedMember.getUser().getId(),
                        savedMember.getUser().getUsername(),
                        savedMember.getRole()
                )
        );

        // Optional: notify the user personally that they were added
        simpMessagingTemplate.convertAndSendToUser(
                savedMember.getUser().getId().toString(),
                "/queue/groups",
                new SimpleChatDTO(chatId, savedMember.getChat().getName(), true)
        );

        return savedMember;
    }


    @Override
    public void removeMemberFromGroup(UUID chatId, UUID userId) {
       /* ChatMember member = chatMemberRepo.findByChatIdAndUserId(chatId, userId);

        member.setIsActive(false);
        member.setLeftAt(LocalDateTime.now());
        chatMemberRepo.save(member);*/
    }

    public List<ChatMember> findByUserId(UUID userId) {
        return chatMemberRepo.findByUserId(userId);
    }

    public List<ChatMember> findByChatId(UUID chatId) {return chatMemberRepo.findByChatId(chatId);}
}
