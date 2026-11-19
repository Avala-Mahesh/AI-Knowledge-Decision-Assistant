package com.aiassistant.exception;

public class DocumentLimitExceededException extends RuntimeException {
    public DocumentLimitExceededException(String message) {
        super(message);
    }
}
