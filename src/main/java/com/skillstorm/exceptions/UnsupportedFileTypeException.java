package com.skillstorm.exceptions;

public class UnsupportedFileTypeException extends IllegalArgumentException {
    public UnsupportedFileTypeException(String message) {
        super(message);
    }
}
