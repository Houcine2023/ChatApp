package com.SummerProject.Skote.Repositories;

import com.SummerProject.Skote.models.Message;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface MessageRepo extends JpaRepository<Message, UUID> {
    List<Message> findByChatMember_ChatIdOrderByCreatedAtAsc(UUID chatId);
}