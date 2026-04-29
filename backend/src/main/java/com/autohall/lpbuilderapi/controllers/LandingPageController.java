package com.autohall.lpbuilderapi.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.autohall.lpbuilderapi.entities.LandingPage;
import com.autohall.lpbuilderapi.services.LandingPageService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/v1/landing-pages")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // Important pour permettre à ton futur React de parler au Java
public class LandingPageController {

    private final LandingPageService landingPageService;

    @GetMapping
    public List<LandingPage> getAllPages() {
        return landingPageService.getAllPages();
    }

    @GetMapping("/{id}")
    public ResponseEntity<LandingPage> getPageById(@PathVariable UUID id) {
        return landingPageService.getPageById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/slug/{slug}")
    public ResponseEntity<LandingPage> getPageBySlug(@PathVariable String slug) {
        return landingPageService.getPageBySlug(slug)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<LandingPage> createPage(@RequestBody LandingPage landingPage) {
        return new ResponseEntity<>(landingPageService.savePage(landingPage), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<LandingPage> updatePage(@PathVariable UUID id, @RequestBody LandingPage landingPage) {
        System.out.println("[DEBUG] PUT /api/v1/landing-pages/" + id + " received. Title: " + landingPage.getTitle() + ", Content keys: " + (landingPage.getContent() != null ? landingPage.getContent().keySet() : "null"));
        LandingPage updated = landingPageService.updateLandingPage(id, landingPage);
        System.out.println("[DEBUG] Update successful. ID: " + updated.getId());
        return ResponseEntity.ok(updated);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePage(@PathVariable UUID id) {
        landingPageService.deletePage(id);
        return ResponseEntity.noContent().build();
    }
}