package com.autohall.lpbuilderapi.controllers;

import com.autohall.lpbuilderapi.config.AdminApiKeyInterceptor;
import com.autohall.lpbuilderapi.config.WebConfig;
import com.autohall.lpbuilderapi.dto.LandingPageResponse;
import com.autohall.lpbuilderapi.exceptions.GlobalExceptionHandler;
import com.autohall.lpbuilderapi.services.LandingPageService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(LandingPageController.class)
@Import({WebConfig.class, AdminApiKeyInterceptor.class, GlobalExceptionHandler.class})
@TestPropertySource(properties = "app.security.admin-api-key=test-admin-key")
class LandingPageControllerTest {

    private static final String ADMIN_HEADER = "X-Admin-Api-Key";
    private static final String ADMIN_KEY = "test-admin-key";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private LandingPageService landingPageService;

    @Test
    void getAllPages_withoutAdminApiKey_shouldReturn401() throws Exception {
        mockMvc.perform(get("/api/v1/landing-pages"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getAllPages_withValidAdminApiKey_shouldReturn200() throws Exception {
        when(landingPageService.getAllPages()).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/landing-pages")
                        .header(ADMIN_HEADER, ADMIN_KEY))
                .andExpect(status().isOk());
    }

    @Test
    void createPage_withInvalidRequest_shouldReturn400() throws Exception {
        String body = """
                {
                  "title": "",
                  "slug": "Bad Slug",
                  "description": "Description",
                  "status": "DRAFT",
                  "content": {}
                }
                """;

        mockMvc.perform(post("/api/v1/landing-pages")
                        .header(ADMIN_HEADER, ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.timestamp").exists())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.path").value("/api/v1/landing-pages"))
                .andExpect(jsonPath("$.validationErrors.title").exists())
                .andExpect(jsonPath("$.validationErrors.slug").exists());
    }

    @Test
    void createPage_withValidRequest_shouldReturn201() throws Exception {
        LandingPageResponse response = response(UUID.randomUUID(), "promo-auto", "DRAFT");
        when(landingPageService.createPage(any())).thenReturn(response);

        mockMvc.perform(post("/api/v1/landing-pages")
                        .header(ADMIN_HEADER, ADMIN_KEY)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.slug").value("promo-auto"))
                .andExpect(jsonPath("$.status").value("DRAFT"));
    }

    @Test
    void deletePage_withValidAdminApiKey_shouldReturn204() throws Exception {
        UUID pageId = UUID.randomUUID();

        mockMvc.perform(delete("/api/v1/landing-pages/{id}", pageId)
                        .header(ADMIN_HEADER, ADMIN_KEY))
                .andExpect(status().isNoContent());

        verify(landingPageService).deletePage(pageId);
    }

    private String validRequestJson() {
        return """
                {
                  "title": "Promo Auto",
                  "slug": "promo-auto",
                  "description": "Description",
                  "status": "DRAFT",
                  "content": {
                    "hero_image": "https://example.com/car.jpg"
                  }
                }
                """;
    }

    private LandingPageResponse response(UUID id, String slug, String status) {
        return new LandingPageResponse(
                id,
                "Promo Auto",
                slug,
                "Description",
                status,
                Map.of("hero_image", "https://example.com/car.jpg"),
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}
