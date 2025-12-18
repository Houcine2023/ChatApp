package com.SummerProject.Skote.DTO;

import java.time.LocalDateTime;
import java.util.UUID;
public record ChatMessageDTO(
        UUID id,
        UUID chatId,
        UUID chatMemberId,
        String content,
        String message,
        MessageType messageType,
        LocalDateTime createdAt,
        LocalDateTime updatedAt,
        boolean isRead,
        boolean isEdited,
        boolean isDeleted,
        UUID userId,
        String name,
        String fileUrl,   // optional for IMAGE or FILE
        String fileName,  // optional
        String fileType   // optional
) {}
