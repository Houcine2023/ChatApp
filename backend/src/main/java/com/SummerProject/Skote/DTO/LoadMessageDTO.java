package com.SummerProject.Skote.DTO;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
public class LoadMessageDTO {

    private UUID messageId;
    private String content;
    private UUID senderId;
    private String senderName;
    private LocalDateTime createdAt;
    private MessageType messageType;
    private String fileUrl;
}
