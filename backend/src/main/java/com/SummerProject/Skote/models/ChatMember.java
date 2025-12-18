package com.SummerProject.Skote.models;

import com.SummerProject.Skote.DTO.Role;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "chat_members",
        uniqueConstraints = @UniqueConstraint(columnNames = {"chat_id", "user_id"}))
@Getter
@Setter
@AllArgsConstructor
public class ChatMember {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_id", nullable = false)
    private Chat chat;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(name = "joined_at", nullable = false)
    private LocalDateTime joinedAt;

    @Column(name = "left_at")
    private LocalDateTime leftAt;

    @Column(nullable = false)
    private Boolean isActive;

    @OneToMany(mappedBy = "chatMember", cascade = CascadeType.ALL)
    private Set<Message> messages;


    public ChatMember() {
        this.joinedAt = LocalDateTime.now();
        this.isActive = true;
        this.role = Role.MEMBER; // Default role
    }
}