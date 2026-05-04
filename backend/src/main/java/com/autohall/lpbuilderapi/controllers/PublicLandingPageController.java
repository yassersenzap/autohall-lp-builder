package com.autohall.lpbuilderapi.controllers;

import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.services.LandingPageService;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/public/landing-pages")
@RequiredArgsConstructor
@Validated
public class PublicLandingPageController {

    private final LandingPageService landingPageService;

    @GetMapping("/{slug}")
    public ResponseEntity<LandingPageResponse> getPublishedPageBySlug(
            @PathVariable
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid slug")
            String slug
    ) {
        return landingPageService.getPublishedPageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
