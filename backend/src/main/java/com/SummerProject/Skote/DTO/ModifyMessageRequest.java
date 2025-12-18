package com.SummerProject.Skote.DTO;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
public class ModifyMessageRequest {

    private UUID chatId;
    private UUID messageId;
    private String newContent;
    private String userId;
}
