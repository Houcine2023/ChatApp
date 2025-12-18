package com.SummerProject.Skote.Repositories;

import com.SummerProject.Skote.models.ChatMember;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ChatMemberRepo extends JpaRepository<ChatMember, UUID> {
    boolean existsByChatIdAndUserId(UUID chatId, UUID userId);
    List<ChatMember> findByChatId(UUID chatId);
    List<ChatMember> findByUserId(UUID userId);
    Optional<ChatMember> findByChatIdAndUserId(UUID chatId, UUID userId);


}