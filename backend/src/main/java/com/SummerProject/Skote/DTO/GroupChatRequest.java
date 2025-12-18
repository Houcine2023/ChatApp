package com.SummerProject.Skote.DTO;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;


@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class GroupChatRequest {
    @NotNull
    private String groupName;
    @NotNull
    private UUID createdById;

    private List<UUID> members;
}
