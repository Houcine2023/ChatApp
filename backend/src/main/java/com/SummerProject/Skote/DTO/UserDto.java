package com.SummerProject.Skote.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record UserDto(
        @NotBlank(message = "Username is required")
        @Size(min = 3, max = 20, message = "Username must be between 3 and 20 characters")
        String username,
        @NotBlank(message = "Password is required")
        String password,
        @NotBlank(message = "Email is required")
        @Email(message = "Email should be valid")
        String email,
        String profilePictureUrl,
        Instant lastSeen,
        Instant updatedAt


) {
}
