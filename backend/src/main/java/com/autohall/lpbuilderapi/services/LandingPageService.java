package com.autohall.lpbuilderapi.services;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
     * Supprime une page.
     */
    @Transactional
    public void deletePage(UUID id) {
        landingPageRepository.deleteById(id);
    }
}