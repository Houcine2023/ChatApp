package com.SummerProject.Skote.events;

import com.SummerProject.Skote.DTO.StatusMessage;
import com.SummerProject.Skote.Services.PresenceService;
import com.SummerProject.Skote.models.User;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Component
public class PresenceEventListener {

    private final PresenceService presenceService;
    private final SimpMessagingTemplate messagingTemplate;

    public PresenceEventListener(PresenceService presenceService,
                                 SimpMessagingTemplate messagingTemplate) {
        this.presenceService = presenceService;
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());

        Object nativeHeadersObj = accessor.getMessageHeaders().get("nativeHeaders");
        if (nativeHeadersObj instanceof Map) {
            @SuppressWarnings("unchecked")
            Map<String, List<String>> nativeHeaders = (Map<String, List<String>>) nativeHeadersObj;

            List<String> userIdList = nativeHeaders.get("userId");
            if (userIdList != null && !userIdList.isEmpty()) {
                try {
                    UUID userId = UUID.fromString(userIdList.get(0).trim());

                    accessor.getSessionAttributes().put("userId", userId);
                    presenceService.setUserOnline(userId);
                    User user = presenceService.getUserById(userId);

                    // Broadcast single user status to everyone
                    StatusMessage statusMsg = new StatusMessage();
                    statusMsg.setUserId(userId);
                    statusMsg.setOnline(true);
                    statusMsg.setUsername(user.getUsername());
                    statusMsg.setProfilePictureUrl(user.getProfilePictureUrl());
                    messagingTemplate.convertAndSend("/topic/status", statusMsg);

                    //  Broadcast full presence list to all users
                    messagingTemplate.convertAndSend("/topic/online-users",
                            presenceService.getAllUsersStatus());


                } catch (IllegalArgumentException e) {
                    System.out.println("[CONNECT EVENT] Invalid UUID format: " + userIdList.get(0));
                }
            } else {
                System.out.println("[CONNECT EVENT] No userId header found.");
            }
        } else {
            System.out.println("[CONNECT EVENT] No nativeHeaders found.");
        }
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(event.getMessage());
        Object userIdObj = accessor.getSessionAttributes().get("userId");

        if (userIdObj instanceof UUID) {
            UUID userId = (UUID) userIdObj;
            presenceService.setUserOffline(userId);
            User user = presenceService.getUserById(userId);

            // Notify single user status
            StatusMessage statusMsg = new StatusMessage();
            statusMsg.setUserId(userId);
            statusMsg.setOnline(false);
            statusMsg.setUsername(user.getUsername());
            statusMsg.setProfilePictureUrl(user.getProfilePictureUrl());
            messagingTemplate.convertAndSend("/topic/status", statusMsg);

            // --- NEW: Broadcast full presence list to all users ---
            messagingTemplate.convertAndSend("/topic/online-users",
                    presenceService.getAllUsersStatus());

        } else {
            System.out.println("[DISCONNECT EVENT] No userId found in session attributes.");
        }
    }
}
