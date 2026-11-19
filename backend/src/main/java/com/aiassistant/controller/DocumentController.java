package com.aiassistant.controller;

import com.aiassistant.dto.DocumentResponse;
import com.aiassistant.entity.User;
import com.aiassistant.service.DocumentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/documents")
@Tag(name = "Knowledge Base Management", description = "Endpoints for uploading, listing, and deleting organization PDF documents")
@SecurityRequirement(name = "bearerAuth")
public class DocumentController {

    private final DocumentService documentService;

    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "Upload a PDF document to the organization's knowledge base (Max 10 docs, 20 pages, 10MB)")
    public ResponseEntity<DocumentResponse> uploadDocument(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) {

        Long orgId = user.getOrganization().getId();
        DocumentResponse response = documentService.uploadDocument(orgId, file);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping
    @Operation(summary = "List all documents in the authenticated organization's knowledge base")
    public ResponseEntity<List<DocumentResponse>> getDocuments(@AuthenticationPrincipal User user) {
        Long orgId = user.getOrganization().getId();
        List<DocumentResponse> documents = documentService.getDocuments(orgId);
        return ResponseEntity.ok(documents);
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a document and its knowledge chunks from the organization's knowledge base")
    public ResponseEntity<Map<String, String>> deleteDocument(
            @AuthenticationPrincipal User user,
            @PathVariable("id") Long documentId) {

        Long orgId = user.getOrganization().getId();
        documentService.deleteDocument(orgId, documentId);
        return ResponseEntity.ok(Map.of(
                "status", "SUCCESS",
                "message", "Document ID " + documentId + " and associated chunks were successfully deleted."
        ));
    }
}
