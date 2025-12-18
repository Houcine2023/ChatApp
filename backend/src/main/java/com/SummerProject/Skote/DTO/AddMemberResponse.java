package com.SummerProject.Skote.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class AddMemberResponse {

    private UUID userId;
    private UUID chatId;
    private Role role;
}
