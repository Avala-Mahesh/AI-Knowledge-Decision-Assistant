package com.aiassistant.dto;

import java.time.LocalDateTime;

public class DocumentResponse {
    private Long id;
    private String fileName;
    private String originalFileName;
    private Long fileSize;
    private Integer pageCount;
    private Integer chunkCount;
    private LocalDateTime uploadedAt;

    public DocumentResponse() {}

    public DocumentResponse(Long id, String fileName, String originalFileName, Long fileSize, Integer pageCount, Integer chunkCount, LocalDateTime uploadedAt) {
        this.id = id;
        this.fileName = fileName;
        this.originalFileName = originalFileName;
        this.fileSize = fileSize;
        this.pageCount = pageCount;
        this.chunkCount = chunkCount;
        this.uploadedAt = uploadedAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getOriginalFileName() { return originalFileName; }
    public void setOriginalFileName(String originalFileName) { this.originalFileName = originalFileName; }

    public Long getFileSize() { return fileSize; }
    public void setFileSize(Long fileSize) { this.fileSize = fileSize; }

    public Integer getPageCount() { return pageCount; }
    public void setPageCount(Integer pageCount) { this.pageCount = pageCount; }

    public Integer getChunkCount() { return chunkCount; }
    public void setChunkCount(Integer chunkCount) { this.chunkCount = chunkCount; }

    public LocalDateTime getUploadedAt() { return uploadedAt; }
    public void setUploadedAt(LocalDateTime uploadedAt) { this.uploadedAt = uploadedAt; }
}
