package com.SummerProject.Skote.models;

import com.SummerProject.Skote.DTO.MessageType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.awt.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "messages")
@Getter
@Setter
@AllArgsConstructor
public class Message {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_member_id", nullable = false)
    private ChatMember chatMember;

    @Column(nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "message_type", nullable = false)
    private MessageType messageType;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private Boolean isRead;
    @Column(nullable = false)
    private Boolean isEdited = false;

    @Column(nullable = false)
    private Boolean isDeleted = false;
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    @Column
    private String fileUrl; // for IMAGE or FILE (stored path or S3/Cloudinary URL)
    @Column
    private String fileName; // optional, for files
    @Column
    private String fileType; // MIME type (e.g., "image/png", "application/pdf")

    public Message() {
        this.createdAt = LocalDateTime.now();
        this.isRead = false;
        this.isDeleted = false;
    }
}