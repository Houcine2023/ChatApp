package com.SummerProject.Skote.abstracts;

import com.SummerProject.Skote.models.ChatMember;

import java.util.UUID;

public interface ChatMemberService {

    ChatMember getChatMember(UUID uuid);
    ChatMember getChatMember(String name);
    ChatMember addMemberToGroup(UUID chatId, UUID userId);
    void removeMemberFromGroup(UUID chatId, UUID userId);

}
