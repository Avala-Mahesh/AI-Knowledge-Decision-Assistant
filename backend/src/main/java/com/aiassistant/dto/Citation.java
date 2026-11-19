package com.aiassistant.dto;

public class Citation {
    private String documentName;
    private Integer pageNumber;
    private String snippet;

    public Citation() {}

    public Citation(String documentName, Integer pageNumber, String snippet) {
        this.documentName = documentName;
        this.pageNumber = pageNumber;
        this.snippet = snippet;
    }

    public String getFormattedReference() {
        return documentName + " — Page " + pageNumber;
    }

    public String getDocumentName() { return documentName; }
    public void setDocumentName(String documentName) { this.documentName = documentName; }

    public Integer getPageNumber() { return pageNumber; }
    public void setPageNumber(Integer pageNumber) { this.pageNumber = pageNumber; }

    public String getSnippet() { return snippet; }
    public void setSnippet(String snippet) { this.snippet = snippet; }
}
