package com.SummerProject.Skote.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class StatusMessage {
    private UUID userId;   // Use UUID to uniquely identify users
    private boolean online;
    private String username;
    private String profilePictureUrl;
    private Instant lastSeen;

}
