package com.aiassistant.controller;

import com.aiassistant.dto.ChatLogResponse;
import com.aiassistant.dto.DashboardStatsResponse;
import com.aiassistant.entity.User;
import com.aiassistant.service.StatsService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dashboard")
@Tag(name = "Organization Dashboard", description = "Endpoints for usage metrics, counts, and recent question logs")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final StatsService statsService;

    public DashboardController(StatsService statsService) {
        this.statsService = statsService;
    }

    @GetMapping("/stats")
    @Operation(summary = "Get organization dashboard statistics and usage counts")
    public ResponseEntity<DashboardStatsResponse> getStats(@AuthenticationPrincipal User user) {
        Long orgId = user.getOrganization().getId();
        return ResponseEntity.ok(statsService.getStats(orgId));
    }

    @GetMapping("/recent-questions")
    @Operation(summary = "Get recent questions asked to the assistant with decision logs and responses")
    public ResponseEntity<List<ChatLogResponse>> getRecentQuestions(
            @AuthenticationPrincipal User user,
            @RequestParam(value = "limit", defaultValue = "10") int limit) {
        Long orgId = user.getOrganization().getId();
        return ResponseEntity.ok(statsService.getRecentChatLogs(orgId, limit));
    }
}
