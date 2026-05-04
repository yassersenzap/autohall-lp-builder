package com.autohall.lpbuilderapi.controllers;

import com.autohall.lpbuilderapi.config.AdminApiKeyInterceptor;
import com.autohall.lpbuilderapi.config.WebConfig;
import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.exceptions.GlobalExceptionHandler;
import com.autohall.lpbuilderapi.services.LandingPageService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(PublicLandingPageController.class)
@Import({WebConfig.class, AdminApiKeyInterceptor.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.security.admin-api-key=test-admin-key")
class PublicLandingPageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LandingPageService landingPageService;

    @Test
    void getPublishedPageBySlug_withoutAdminApiKey_shouldBeAccessible() throws Exception {
        when(landingPageService.getPublishedPageBySlug("promo-auto")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/public/landing-pages/{slug}", "promo-auto"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getPublishedPageBySlug_whenPageExists_shouldReturn200() throws Exception {
        when(landingPageService.getPublishedPageBySlug("promo-auto"))
                .thenReturn(Optional.of(response("promo-auto")));

        mockMvc.perform(get("/api/v1/public/landing-pages/{slug}", "promo-auto"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.slug").value("promo-auto"))
                .andExpect(jsonPath("$.status").value("PUBLISHED"));
    }

    @Test
    void getPublishedPageBySlug_whenPageDoesNotExist_shouldReturn404() throws Exception {
        when(landingPageService.getPublishedPageBySlug("missing-page")).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/v1/public/landing-pages/{slug}", "missing-page"))
                .andExpect(status().isNotFound());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Tiggo 8", "tiggo_8", "tiggo--8"})
    void getPublishedPageBySlug_withInvalidSlug_shouldReturn400(String slug) throws Exception {
        mockMvc.perform(get("/api/v1/public/landing-pages/{slug}", slug))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").exists())
                .andExpect(jsonPath("$.validationErrors").exists());
    }

    private LandingPageResponse response(String slug) {
        return new LandingPageResponse(
                UUID.randomUUID(),
                "Promo Auto",
                slug,
                "Description",
                "PUBLISHED",
                Map.of("hero_image", "https://example.com/car.jpg"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
