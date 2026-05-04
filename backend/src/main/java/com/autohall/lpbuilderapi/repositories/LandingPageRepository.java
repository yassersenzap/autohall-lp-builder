package com.autohall.lpbuilderapi.repositories;

import com.autohall.lpbuilderapi.entities.LandingPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface LandingPageRepository extends JpaRepository<LandingPage, UUID> {

    Optional<LandingPage> findBySlug(String slug);

    Optional<LandingPage> findBySlugAndStatus(String slug, String status);

    boolean existsBySlug(String slug);

    boolean existsBySlugAndIdNot(String slug, UUID id);
}
