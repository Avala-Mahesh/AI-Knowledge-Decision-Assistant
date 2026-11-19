package com.aiassistant.dto;

import java.util.List;

public class AskResponse {
    private String answer;
    private List<Citation> references;
    private String status; // "ANSWERED", "UNABLE_TO_ANSWER", "ERROR"
    private PipelineTrace pipelineTrace;

    public AskResponse() {}

    public AskResponse(String answer, List<Citation> references, String status, PipelineTrace pipelineTrace) {
        this.answer = answer;
        this.references = references;
        this.status = status;
        this.pipelineTrace = pipelineTrace;
    }

    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }

    public List<Citation> getReferences() { return references; }
    public void setReferences(List<Citation> references) { this.references = references; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public PipelineTrace getPipelineTrace() { return pipelineTrace; }
    public void setPipelineTrace(PipelineTrace pipelineTrace) { this.pipelineTrace = pipelineTrace; }
}
