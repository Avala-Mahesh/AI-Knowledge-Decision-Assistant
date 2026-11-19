package com.aiassistant.controller;

import com.aiassistant.dto.AskRequest;
import com.aiassistant.dto.AskResponse;
import com.aiassistant.entity.Organization;
import com.aiassistant.exception.ResourceNotFoundException;
import com.aiassistant.repository.OrganizationRepository;
import com.aiassistant.service.QuestionPipelineService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/public")
@Tag(name = "Public Visitor Assistant", description = "Public endpoints for visitors to ask questions without authentication")
public class ChatController {

    private final QuestionPipelineService questionPipelineService;
    private final OrganizationRepository organizationRepository;

    public ChatController(
            QuestionPipelineService questionPipelineService,
            OrganizationRepository organizationRepository) {
        this.questionPipelineService = questionPipelineService;
        this.organizationRepository = organizationRepository;
    }

    @PostMapping("/ask")
    @Operation(summary = "Ask a natural-language question using an organization's assistant token")
    public ResponseEntity<AskResponse> askQuestion(@Valid @RequestBody AskRequest request) {
        AskResponse response = questionPipelineService.processQuestion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/assistant-info/{token}")
    @Operation(summary = "Get public assistant metadata for visitor chat interface")
    public ResponseEntity<Map<String, Object>> getAssistantPublicInfo(@PathVariable("token") String token) {
        Organization org = organizationRepository.findByAssistantToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Assistant not found for token: " + token));

        return ResponseEntity.ok(Map.of(
                "organizationName", org.getName(),
                "organizationSlug", org.getSlug(),
                "assistantToken", org.getAssistantToken(),
                "status", "ACTIVE"
        ));
    }
}
