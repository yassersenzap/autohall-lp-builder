package com.autohall.lpbuilderapi.services;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.autohall.lpbuilderapi.entities.LandingPage;
import com.autohall.lpbuilderapi.repositories.LandingPageRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LandingPageService {

    private final LandingPageRepository landingPageRepository;

    /**
     * Récupère toutes les pages créées pour Auto Hall.
     */
    public List<LandingPage> getAllPages() {
        return landingPageRepository.findAll();
    }

    /**
     * Récupère une page spécifique par son ID technique.
     */
    public Optional<LandingPage> getPageById(UUID id) {
        return landingPageRepository.findById(id);
    }

    /**
     * Récupère une page par son slug (URL). 
     * C'est ce que React appellera pour afficher la page au public.
     */
    public Optional<LandingPage> getPageBySlug(String slug) {
        return landingPageRepository.findBySlug(slug);
    }

    /**
     * Sauvegarde ou met à jour une page.
     * Ici, on pourrait ajouter une logique de validation (ex: vérifier si le slug est unique).
     */
    @Transactional
    public LandingPage savePage(LandingPage landingPage) {
        // Logique métier : On peut forcer le statut en DRAFT par défaut si vide
        if (landingPage.getStatus() == null) {
            landingPage.setStatus("DRAFT");
        }
        return landingPageRepository.save(landingPage);
    }

    /**
     * Met à jour une page existante en fusionnant le contenu JSONB.
     * Seuls les champs fournis sont mis à jour ; le contenu existant est préservé et merge.
     */
    @Transactional
    public LandingPage updateLandingPage(UUID id, LandingPage details) {
        System.out.println("[DEBUG] Service updateLandingPage called for id: " + id + ", title: " + details.getTitle());
        LandingPage existing = landingPageRepository.findById(id)
                .orElseThrow(() -> {
                    System.out.println("[DEBUG] Page not found with id: " + id);
                    return new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
                });

        System.out.println("[DEBUG] Existing page title: " + existing.getTitle() + ", content: " + existing.getContent());
        System.out.println("[DEBUG] New content to merge: " + details.getContent());

        // Update simple fields
        existing.setTitle(details.getTitle());
        existing.setSlug(details.getSlug());
        existing.setDescription(details.getDescription());
        existing.setStatus(details.getStatus());

        // Merge content JSONB: preserve existing keys not overridden
        Map<String, Object> newContent = details.getContent();
        if (newContent != null) {
            Map<String, Object> existingContent = existing.getContent();
            if (existingContent == null) {
                existing.setContent(new HashMap<>(newContent));
                System.out.println("[DEBUG] Content was null, set to new content");
            } else {
                existingContent.putAll(newContent);
                System.out.println("[DEBUG] Merged new content into existing. Result: " + existing.getContent());
            }
        } else {
            System.out.println("[DEBUG] New content is null, skipping merge");
        }

        // saved via @PreUpdate hook
        LandingPage saved = landingPageRepository.save(existing);
        System.out.println("[DEBUG] Page saved. updatedAt: " + saved.getUpdatedAt());
        return saved;
    }

    /**
     * Supprime une page.
     */
    @Transactional
    public void deletePage(UUID id) {
        landingPageRepository.deleteById(id);
    }
}