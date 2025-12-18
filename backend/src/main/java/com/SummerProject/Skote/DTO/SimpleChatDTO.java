package com.SummerProject.Skote.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SimpleChatDTO {
    private UUID chatId;
    private String name;
    private boolean isGroup;

}
