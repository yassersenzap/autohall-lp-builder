package com.autohall.lpbuilderapi.controllers;

import com.autohall.lpbuilderapi.dto.LandingPageRequest;
import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.services.LandingPageService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/landing-pages")
@RequiredArgsConstructor
@Validated
public class LandingPageController {

    private final LandingPageService landingPageService;

    @GetMapping
    public List<LandingPageResponse> getAllPages() {
        return landingPageService.getAllPages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandingPageResponse> getPageById(@PathVariable UUID id) {
        return landingPageService.getPageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<LandingPageResponse> getPublishedPageBySlug(
            @PathVariable
            @Pattern(regexp = "^[a-z0-9]+(?:-[a-z0-9]+)*$", message = "Invalid slug")
            String slug
    ) {
        return landingPageService.getPublishedPageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LandingPageResponse> createPage(@Valid @RequestBody LandingPageRequest request) {
        return new ResponseEntity<>(landingPageService.createPage(request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandingPageResponse> updatePage(@PathVariable UUID id, @Valid @RequestBody LandingPageRequest request) {
        return ResponseEntity.ok(landingPageService.updateLandingPage(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable UUID id) {
        landingPageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }
}
