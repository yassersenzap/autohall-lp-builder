package com.autohall.lpbuilderapi.repositories;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.autohall.lpbuilderapi.entities.LandingPage;

/**
 * Interface Repository pour la gestion des Landing Pages.
 * Elle hérite de JpaRepository pour offrir toutes les opérations CRUD de base.
 */
@Repository
public interface LandingPageRepository extends JpaRepository<LandingPage, UUID> {

    /**
     * Permet de retrouver une page via son slug (URL).
     * Essentiel pour l'affichage dynamique côté Frontend.
     */
    Optional<LandingPage> findBySlug(String slug);

    /**
     * Vérifie l'existence d'un slug pour éviter les doublons d'URL.
     */
    boolean existsBySlug(String slug);
}