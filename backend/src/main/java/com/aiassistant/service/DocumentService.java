package com.aiassistant.service;

import com.aiassistant.dto.DocumentResponse;
import com.aiassistant.entity.Document;
import com.aiassistant.entity.DocumentChunk;
import com.aiassistant.entity.Organization;
import com.aiassistant.exception.DocumentLimitExceededException;
import com.aiassistant.exception.InvalidDocumentException;
import com.aiassistant.exception.ResourceNotFoundException;
import com.aiassistant.repository.DocumentChunkRepository;
import com.aiassistant.repository.DocumentRepository;
import com.aiassistant.repository.OrganizationRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DocumentService {

    private static final Logger logger = LoggerFactory.getLogger(DocumentService.class);
    private static final int MAX_DOCUMENTS_PER_ORG = 10;
    private static final long MAX_FILE_SIZE_BYTES = 10 * 1024 * 1024; // 10 MB

    private final DocumentRepository documentRepository;
    private final DocumentChunkRepository documentChunkRepository;
    private final OrganizationRepository organizationRepository;
    private final PdfExtractionService pdfExtractionService;

    public DocumentService(
            DocumentRepository documentRepository,
            DocumentChunkRepository documentChunkRepository,
            OrganizationRepository organizationRepository,
            PdfExtractionService pdfExtractionService) {
        this.documentRepository = documentRepository;
        this.documentChunkRepository = documentChunkRepository;
        this.organizationRepository = organizationRepository;
        this.pdfExtractionService = pdfExtractionService;
    }

    @Transactional
    public DocumentResponse uploadDocument(Long organizationId, MultipartFile file) {
        // 1. Organization lookup
        Organization organization = organizationRepository.findById(organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Organization not found with ID: " + organizationId));

        // 2. Validate Document Count Limit (Max 10 per org)
        long currentDocCount = documentRepository.countByOrganizationId(organizationId);
        if (currentDocCount >= MAX_DOCUMENTS_PER_ORG) {
            throw new DocumentLimitExceededException("Organization has reached the maximum limit of " + MAX_DOCUMENTS_PER_ORG + " documents. Please delete an existing document before uploading a new one.");
        }

        // 3. Validate File Presence & Size Limit (Max 10 MB)
        if (file == null || file.isEmpty()) {
            throw new InvalidDocumentException("No file provided or file is empty.");
        }
        if (file.getSize() > MAX_FILE_SIZE_BYTES) {
            throw new DocumentLimitExceededException("File size (" + (file.getSize() / (1024 * 1024)) + " MB) exceeds the maximum limit of 10 MB per PDF.");
        }

        // 4. Validate Content Type and Name
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.toLowerCase().endsWith(".pdf")) {
            throw new InvalidDocumentException("Only PDF documents (.pdf) are allowed.");
        }

        byte[] fileBytes;
        try {
            fileBytes = file.getBytes();
        } catch (IOException e) {
            throw new InvalidDocumentException("Failed to read file content: " + e.getMessage());
        }

        // 5. Process PDF & Extract Text with Page Numbers (Validates max 20 pages limit inside)
        PdfExtractionService.PdfExtractionResult extractionResult = pdfExtractionService.processPdf(fileBytes, originalFilename);

        // 6. Persist Document record
        String storedFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Document document = new Document(
                organization,
                storedFileName,
                originalFilename,
                file.getSize(),
                extractionResult.getPageCount(),
                extractionResult.getChunks().size()
        );
        Document savedDoc = documentRepository.save(document);

        // 7. Persist Chunks with organization scoping & page numbers
        List<DocumentChunk> chunkEntities = new ArrayList<>();
        for (PdfExtractionService.ExtractedChunk chunk : extractionResult.getChunks()) {
            chunkEntities.add(new DocumentChunk(
                    organization,
                    savedDoc,
                    chunk.getChunkIndex(),
                    chunk.getPageNumber(),
                    chunk.getContent()
            ));
        }
        documentChunkRepository.saveAll(chunkEntities);

        logger.info("Successfully uploaded and indexed document '{}' (ID: {}) for Org ID: {}. Pages: {}, Chunks: {}",
                originalFilename, savedDoc.getId(), organizationId, extractionResult.getPageCount(), chunkEntities.size());

        return mapToResponse(savedDoc);
    }

    @Transactional(readOnly = true)
    public List<DocumentResponse> getDocuments(Long organizationId) {
        return documentRepository.findByOrganizationIdOrderByUploadedAtDesc(organizationId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteDocument(Long organizationId, Long documentId) {
        Document document = documentRepository.findByIdAndOrganizationId(documentId, organizationId)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found with ID: " + documentId + " for this organization."));

        // Delete all associated chunks first
        documentChunkRepository.deleteByDocumentId(document.getId());

        // Delete document record
        documentRepository.delete(document);
        logger.info("Deleted document ID: {} and its chunks for Org ID: {}", documentId, organizationId);
    }

    private DocumentResponse mapToResponse(Document doc) {
        return new DocumentResponse(
                doc.getId(),
                doc.getFileName(),
                doc.getOriginalFileName(),
                doc.getFileSize(),
                doc.getPageCount(),
                doc.getChunkCount(),
                doc.getUploadedAt()
        );
    }
}
