package com.autohall.lpbuilderapi.dto;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record LandingPageResponse(
        UUID id,
        String title,
        String slug,
        String description,
        String status,
        Map<String, Object> content,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
