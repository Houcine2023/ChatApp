package com.SummerProject.Skote.Services;

import com.SummerProject.Skote.Repositories.UserRepo;
import com.SummerProject.Skote.models.User;
import com.SummerProject.Skote.models.UserPresence;
import com.SummerProject.Skote.shared.CustomeResponseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class PresenceService {

    private final Map<UUID, UserPresence> activeUsers = new ConcurrentHashMap<>();

    @Autowired
    UserRepo userrepo;

    public void setUserOnline(UUID userId) {
        if (userId != null) {
            User user = userrepo.findById(userId).orElseThrow(() -> CustomeResponseException.resourceNotFound(
                    "User with Id " + userId + " not found"
            ));
            activeUsers.put(userId, new UserPresence(userId, user, true, Instant.now()));
        }
    }

    public void setUserOffline(UUID userId) {
        if (userId != null) {
            UserPresence presence = activeUsers.get(userId);
            if (presence != null) {
                presence.setOnline(false);
                presence.setLastSeen(Instant.now());
                activeUsers.put(userId, presence);
            }
        }
    }

    public boolean isUserOnline(UUID userId) {
        return activeUsers.getOrDefault(userId, new UserPresence(userId, false, null)).isOnline();
    }

    public Instant getLastSeen(UUID userId) {
        return activeUsers.getOrDefault(userId, new UserPresence(userId, false, null)).getLastSeen();
    }

    public Map<UUID, UserPresence> getAllUsersPresence() {
        return Collections.unmodifiableMap(activeUsers);
    }

    public void removeUser(UUID userId) {
        if (userId != null) {
            activeUsers.remove(userId);
        }
    }

    // --- NEW: Return full status messages for frontend ---
    public Map<UUID, com.SummerProject.Skote.DTO.StatusMessage> getAllUsersStatus() {
        return activeUsers.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> {
                            UserPresence presence = entry.getValue();
                            User user = presence.getUser();
                            return new com.SummerProject.Skote.DTO.StatusMessage(
                                    user.getId(),
                                    presence.isOnline(),
                                    user.getUsername(),
                                    user.getProfilePictureUrl(),
                                    presence.getLastSeen()
                            );
                        }
                ));
    }

    // --- NEW: helper to get User by UUID ---
    public User getUserById(UUID userId) {
        UserPresence presence = activeUsers.get(userId);
        if (presence != null) return presence.getUser();
        return null;
    }
}
