package com.aiassistant.dto;

public class AuthResponse {
    private String token;
    private String type = "Bearer";
    private Long userId;
    private String email;
    private String fullName;
    private Long organizationId;
    private String organizationName;
    private String assistantToken;

    public AuthResponse() {}

    public AuthResponse(String token, Long userId, String email, String fullName, Long organizationId, String organizationName, String assistantToken) {
        this.token = token;
        this.userId = userId;
        this.email = email;
        this.fullName = fullName;
        this.organizationId = organizationId;
        this.organizationName = organizationName;
        this.assistantToken = assistantToken;
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }

    public String getType() { return type; }
    public void setType(String type) { this.type = type; }

    public Long getUserId() { return userId; }
    public void setUserId(Long userId) { this.userId = userId; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public Long getOrganizationId() { return organizationId; }
    public void setOrganizationId(Long organizationId) { this.organizationId = organizationId; }

    public String getOrganizationName() { return organizationName; }
    public void setOrganizationName(String organizationName) { this.organizationName = organizationName; }

    public String getAssistantToken() { return assistantToken; }
    public void setAssistantToken(String assistantToken) { this.assistantToken = assistantToken; }
}
