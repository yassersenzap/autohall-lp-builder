package com.autohall.lpbuilderapi.services;

import com.autohall.lpbuilderapi.dto.LandingPageRequest;
import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.entities.LandingPage;
import com.autohall.lpbuilderapi.repositories.LandingPageRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LandingPageServiceTest {

    @Mock
    private LandingPageRepository landingPageRepository;

    private LandingPageService landingPageService;

    @BeforeEach
    void setUp() {
        landingPageService = new LandingPageService(landingPageRepository, new ObjectMapper());
    }

    @ParameterizedTest
    @NullAndEmptySource
    void createPage_shouldCreateDraftPageByDefault(String status) {
        LandingPageRequest request = validRequest(" Promo-Auto ", status, Map.of("hero_image", "https://example.com/car.jpg"));
        when(landingPageRepository.existsBySlug("promo-auto")).thenReturn(false);
        when(landingPageRepository.save(any(LandingPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LandingPageResponse response = landingPageService.createPage(request);

        assertEquals("DRAFT", response.status());
        ArgumentCaptor<LandingPage> pageCaptor = ArgumentCaptor.forClass(LandingPage.class);
        verify(landingPageRepository).save(pageCaptor.capture());
        assertEquals("DRAFT", pageCaptor.getValue().getStatus());
    }

    @Test
    void createPage_shouldRejectDuplicateSlug() {
        LandingPageRequest request = validRequest("promo-auto", "DRAFT", Map.of("hero_image", "https://example.com/car.jpg"));
        when(landingPageRepository.existsBySlug("promo-auto")).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.createPage(request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(landingPageRepository, never()).save(any(LandingPage.class));
    }

    @Test
    void createPage_shouldNormalizeSlug() {
        LandingPageRequest request = validRequest(" Promo-Auto ", "DRAFT", Map.of("hero_image", "https://example.com/car.jpg"));
        when(landingPageRepository.existsBySlug("promo-auto")).thenReturn(false);
        when(landingPageRepository.save(any(LandingPage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        LandingPageResponse response = landingPageService.createPage(request);

        assertEquals("promo-auto", response.slug());
        ArgumentCaptor<LandingPage> pageCaptor = ArgumentCaptor.forClass(LandingPage.class);
        verify(landingPageRepository).save(pageCaptor.capture());
        assertEquals("promo-auto", pageCaptor.getValue().getSlug());
    }

    @Test
    void updateLandingPage_shouldUpdateExistingPage() {
        UUID pageId = UUID.randomUUID();
        LandingPage existing = LandingPage.builder()
                .id(pageId)
                .title("Old title")
                .slug("old-slug")
                .description("Old description")
                .status("DRAFT")
                .content(new HashMap<>(Map.of(
                        "hero_title", "Old hero",
                        "price", "199000 MAD"
                )))
                .build();
        LandingPageRequest request = validRequest(" New-Slug ", "PUBLISHED", Map.of(
                "hero_title", "New hero",
                "hero_image", "https://example.com/new-car.jpg"
        ));

        when(landingPageRepository.findById(pageId)).thenReturn(Optional.of(existing));
        when(landingPageRepository.existsBySlugAndIdNot("new-slug", pageId)).thenReturn(false);
        when(landingPageRepository.save(existing)).thenReturn(existing);

        LandingPageResponse response = landingPageService.updateLandingPage(pageId, request);

        assertEquals("Promo Auto", response.title());
        assertEquals("new-slug", response.slug());
        assertEquals("Description", response.description());
        assertEquals("PUBLISHED", response.status());
        assertEquals("New hero", response.content().get("hero_title"));
        assertEquals("199000 MAD", response.content().get("price"));
        assertEquals("https://example.com/new-car.jpg", response.content().get("hero_image"));
        verify(landingPageRepository).save(existing);
    }

    @Test
    void updateLandingPage_shouldRejectDuplicateSlugFromAnotherPage() {
        UUID pageId = UUID.randomUUID();
        LandingPage existing = LandingPage.builder()
                .id(pageId)
                .title("Old title")
                .slug("old-slug")
                .status("DRAFT")
                .build();
        LandingPageRequest request = validRequest("used-slug", "DRAFT", Map.of("hero_image", "https://example.com/car.jpg"));

        when(landingPageRepository.findById(pageId)).thenReturn(Optional.of(existing));
        when(landingPageRepository.existsBySlugAndIdNot("used-slug", pageId)).thenReturn(true);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.updateLandingPage(pageId, request));

        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(landingPageRepository, never()).save(any(LandingPage.class));
    }

    @Test
    void getPublishedPageBySlug_shouldReturnOnlyPublishedPage() {
        when(landingPageRepository.findBySlugAndStatus("promo-auto", "PUBLISHED"))
                .thenReturn(Optional.empty());

        assertTrue(landingPageService.getPublishedPageBySlug(" Promo-Auto ").isEmpty());
        verify(landingPageRepository).findBySlugAndStatus("promo-auto", "PUBLISHED");
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "javascript:alert(1)",
            "ftp://example.com/car.jpg",
            "file:///tmp/car.jpg",
            "not a url"
    })
    void validateContent_shouldRejectInvalidHeroImageUrl(String heroImageUrl) {
        LandingPageRequest request = validRequest("promo-auto", "DRAFT", Map.of("hero_image", heroImageUrl));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.createPage(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void validateContent_shouldRejectInvalidPriceWhenPriceIsNotString() {
        LandingPageRequest request = validRequest("promo-auto", "DRAFT", Map.of("price", 250000));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.createPage(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void validateContent_shouldRejectInvalidPriceWhenPriceIsTooLong() {
        LandingPageRequest request = validRequest("promo-auto", "DRAFT", Map.of("price", "1".repeat(101)));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.createPage(request));

        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    void deletePage_shouldThrowNotFoundWhenPageDoesNotExist() {
        UUID pageId = UUID.randomUUID();
        when(landingPageRepository.existsById(pageId)).thenReturn(false);

        ResponseStatusException exception = assertThrows(ResponseStatusException.class,
                () -> landingPageService.deletePage(pageId));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        verify(landingPageRepository, never()).deleteById(any(UUID.class));
    }

    @Test
    void deletePage_shouldDeleteWhenPageExists() {
        UUID pageId = UUID.randomUUID();
        when(landingPageRepository.existsById(pageId)).thenReturn(true);

        landingPageService.deletePage(pageId);

        verify(landingPageRepository).deleteById(pageId);
    }

    private LandingPageRequest validRequest(String slug, String status, Map<String, Object> content) {
        return new LandingPageRequest(
                "Promo Auto",
                slug,
                "Description",
                status,
                content
        );
    }
}
