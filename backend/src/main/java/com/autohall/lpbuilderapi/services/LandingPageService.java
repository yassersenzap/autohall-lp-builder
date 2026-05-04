package com.autohall.lpbuilderapi.services;

import com.autohall.lpbuilderapi.dto.LandingPageRequest;
import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.entities.LandingPage;
import com.autohall.lpbuilderapi.repositories.LandingPageRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LandingPageService {

    private static final String STATUS_DRAFT = "DRAFT";
    private static final String STATUS_PUBLISHED = "PUBLISHED";
    private static final Set<String> ALLOWED_STATUSES = Set.of(STATUS_DRAFT, STATUS_PUBLISHED, "ARCHIVED");
    private static final int MAX_CONTENT_BYTES = 200 * 1024;

    private final LandingPageRepository landingPageRepository;
    private final ObjectMapper objectMapper;

    public List<LandingPageResponse> getAllPages() {
        return landingPageRepository.findAll().stream()
                .map(this::toResponse)
                .toList();
    }

    public Optional<LandingPageResponse> getPageById(UUID id) {
        return landingPageRepository.findById(id).map(this::toResponse);
    }

    public Optional<LandingPageResponse> getPublishedPageBySlug(String slug) {
        return landingPageRepository.findBySlugAndStatus(normalizeSlug(slug), STATUS_PUBLISHED)
                .map(this::toResponse);
    }

    @Transactional
    public LandingPageResponse createPage(LandingPageRequest request) {
        validateRequest(request);
        String slug = normalizeSlug(request.slug());

        if (landingPageRepository.existsBySlug(slug)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        LandingPage landingPage = LandingPage.builder()
                .title(request.title().trim())
                .slug(slug)
                .description(trimToNull(request.description()))
                .status(resolveStatus(request.status(), STATUS_DRAFT))
                .content(copyContent(request.content()))
                .build();

        return toResponse(landingPageRepository.save(landingPage));
    }

    @Transactional
    public LandingPageResponse updateLandingPage(UUID id, LandingPageRequest request) {
        validateRequest(request);
        LandingPage existing = landingPageRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found"));

        String slug = normalizeSlug(request.slug());
        if (!existing.getSlug().equals(slug) && landingPageRepository.existsBySlugAndIdNot(slug, id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Slug already exists");
        }

        existing.setTitle(request.title().trim());
        existing.setSlug(slug);
        existing.setDescription(trimToNull(request.description()));
        existing.setStatus(resolveStatus(request.status(), existing.getStatus()));

        Map<String, Object> newContent = request.content();
        if (newContent != null) {
            Map<String, Object> existingContent = existing.getContent();
            if (existingContent == null) {
                existing.setContent(copyContent(newContent));
            } else {
                // Preserve existing builder blocks when updates only send a partial payload.
                existingContent.putAll(copyContent(newContent));
            }
        }

        return toResponse(landingPageRepository.save(existing));
    }

    @Transactional
    public void deletePage(UUID id) {
        if (!landingPageRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Page not found");
        }
        landingPageRepository.deleteById(id);
    }

    private void validateRequest(LandingPageRequest request) {
        String status = request.status();
        if (StringUtils.hasText(status) && !ALLOWED_STATUSES.contains(status)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid landing page status");
        }

        validateContent(request.content());
    }

    private void validateContent(Map<String, Object> content) {
        if (content == null) {
            return;
        }

        try {
            int size = objectMapper.writeValueAsString(content).getBytes(StandardCharsets.UTF_8).length;
            if (size > MAX_CONTENT_BYTES) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content payload is too large");
            }
        } catch (JsonProcessingException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Content must be valid JSON");
        }

        Object heroImage = content.get("hero_image");
        if (heroImage != null) {
            validateHttpUrl(heroImage, "hero_image");
        }

        Object price = content.get("price");
        if (price != null && (!(price instanceof String value) || value.length() > 100)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "price must be a string with at most 100 characters");
        }
    }

    private void validateHttpUrl(Object value, String fieldName) {
        if (!(value instanceof String url) || url.length() > 2048) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid URL");
        }

        try {
            URI uri = new URI(url);
            String scheme = uri.getScheme();
            if (!StringUtils.hasText(scheme) || (!"http".equalsIgnoreCase(scheme) && !"https".equalsIgnoreCase(scheme))) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must use http or https");
            }
            if (!StringUtils.hasText(uri.getHost())) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must include a host");
            }
        } catch (URISyntaxException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, fieldName + " must be a valid URL");
        }
    }

    private String normalizeSlug(String slug) {
        return slug.trim().toLowerCase();
    }

    private String resolveStatus(String requestedStatus, String fallbackStatus) {
        return StringUtils.hasText(requestedStatus) ? requestedStatus : fallbackStatus;
    }

    private String trimToNull(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        return value.trim();
    }

    private Map<String, Object> copyContent(Map<String, Object> content) {
        return content == null ? null : new HashMap<>(content);
    }

    private LandingPageResponse toResponse(LandingPage landingPage) {
        return new LandingPageResponse(
                landingPage.getId(),
                landingPage.getTitle(),
                landingPage.getSlug(),
                landingPage.getDescription(),
                landingPage.getStatus(),
                landingPage.getContent(),
                landingPage.getCreatedAt(),
                landingPage.getUpdatedAt()
        );
    }
}
