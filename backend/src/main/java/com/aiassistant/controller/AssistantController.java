package com.aiassistant.controller;

import com.aiassistant.dto.DashboardStatsResponse;
import com.aiassistant.entity.User;
import com.aiassistant.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/assistant")
@Tag(name = "Assistant Management", description = "Endpoints for managing the organization's assistant URL, token, and embed code")
@SecurityRequirement(name = "bearerAuth")
public class AssistantController {

    private final StatsService statsService;

    public AssistantController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping
    @Operation(summary = "Get assistant configuration, public URL, and embed codes")
    public ResponseEntity<DashboardStatsResponse> getAssistantInfo(@AuthenticationPrincipal User user) {
        Long orgId = user.getOrganization().getId();
        DashboardStatsResponse stats = statsService.getStats(orgId);
        return ResponseEntity.ok(stats);
    }

    @PostMapping("/regenerate-token")
    @Operation(summary = "Regenerate the organization's assistant token (invalidates existing URLs)")
    public ResponseEntity<Map<String, String>> regenerateToken(@AuthenticationPrincipal User user) {
        Long orgId = user.getOrganization().getId();
        String newToken = statsService.regenerateAssistantToken(orgId);
        DashboardStatsResponse stats = statsService.getStats(orgId);

        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "assistantToken", newToken,
                "assistantUrl", stats.getAssistantUrl(),
                "embedCode", stats.getEmbedCode()
        ));
    }
}
