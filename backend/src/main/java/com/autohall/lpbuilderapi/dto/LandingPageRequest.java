package com.autohall.lpbuilderapi.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.Map;

public record LandingPageRequest(
        @NotBlank(message = "Title is required")
        @Size(max = 160, message = "Title must not exceed 160 characters")
        String title,

        @NotBlank(message = "Slug is required")
        @Size(max = 120, message = "Slug must not exceed 120 characters")
        @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Slug must contain lowercase letters, numbers and single hyphens only")
        String slug,

        @Size(max = 500, message = "Description must not exceed 500 characters")
        String description,

        @Pattern(regexp = "DRAFT|PUBLISHED|ARCHIVED", message = "Status must be DRAFT, PUBLISHED or ARCHIVED")
        String status,

        @Size(max = 50, message = "Content must not contain more than 50 top-level keys")
        Map<String, Object> content
) {
}
