package com.SummerProject.Skote.DTO;

import com.SummerProject.Skote.models.Message;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class MessageRequest {
    private UUID chatId;
    private String content;
    private MessageType messageType;
    private String userId; // frontend sends string
}
