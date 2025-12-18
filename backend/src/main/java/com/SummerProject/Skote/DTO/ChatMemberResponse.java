package com.SummerProject.Skote.DTO;

import java.util.UUID;

public record ChatMemberResponse(UUID id, UUID userId, String username, Role role) {}
