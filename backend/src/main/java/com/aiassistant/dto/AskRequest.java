package com.aiassistant.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AskRequest {

    @NotBlank(message = "Assistant token is required")
    private String assistantToken;

    @NotBlank(message = "Question cannot be empty")
    @Size(max = 1000, message = "Question cannot exceed 1000 characters")
    private String question;

    public AskRequest() {}

    public AskRequest(String assistantToken, String question) {
        this.assistantToken = assistantToken;
        this.question = question;
    }

    public String getAssistantToken() { return assistantToken; }
    public void setAssistantToken(String assistantToken) { this.assistantToken = assistantToken; }

    public String getQuestion() { return question; }
    public void setQuestion(String question) { this.question = question; }
}
