package com.SummerProject.Skote.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class DeleteMessageRequest {

    private UUID chatId;
    private UUID messageId;
    private String userId;
}
