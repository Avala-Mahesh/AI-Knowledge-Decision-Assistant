package com.aiassistant.dto;

import java.time.LocalDateTime;

public class OrganizationResponse {
    private Long id;
    private String name;
    private String slug;
    private String assistantToken;
    private LocalDateTime createdAt;

    public OrganizationResponse() {}

    public OrganizationResponse(Long id, String name, String slug, String assistantToken, LocalDateTime createdAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.assistantToken = assistantToken;
        this.createdAt = createdAt;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSlug() { return slug; }
    public void setSlug(String slug) { this.slug = slug; }

    public String getAssistantToken() { return assistantToken; }
    public void setAssistantToken(String assistantToken) { this.assistantToken = assistantToken; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
