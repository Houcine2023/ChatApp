package com.SummerProject.Skote.abstracts;

import com.SummerProject.Skote.DTO.GroupChatRequest;
import com.SummerProject.Skote.DTO.MessageRequest;
import com.SummerProject.Skote.DTO.MessageType;
import com.SummerProject.Skote.models.Chat;
import com.SummerProject.Skote.models.ChatMember;
import com.SummerProject.Skote.models.Message;

import java.util.List;
import java.util.UUID;

public interface ChatService {
    Chat createGroupChat(GroupChatRequest groupChatRequest);
    Message sendGroupMessage(MessageRequest messageRequest);
    List<ChatMember> findByChatId(UUID chatId);
    List<Message> getGoupMessages(UUID chatId);

}
