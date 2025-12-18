package com.SummerProject.Skote.Repositories;

import com.SummerProject.Skote.models.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsById(UUID uuid);


    // Search users by username or email excluding current user
    @Query("SELECT u FROM User u WHERE (u.username LIKE %:query% OR u.email LIKE %:query%) AND u.id != :userId")
    List<User> findByUsernameOrEmailContaining(@Param("query") String query, @Param("userId") UUID userId);

    // Optional: fetch ChatMember ids for user in groups
    @Query("SELECT cm.id FROM ChatMember cm WHERE cm.user.id = :userId AND cm.chat.id = :chatId AND cm.isActive = true")
    Optional<UUID> findChatMemberId(@Param("userId") UUID userId, @Param("chatId") UUID chatId);
}

