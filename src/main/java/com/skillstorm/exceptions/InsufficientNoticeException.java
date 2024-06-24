package com.skillstorm.exceptions;

public class InsufficientNoticeException extends IllegalArgumentException {
    public InsufficientNoticeException(String message) {
        super(message);
    }
}
