package com.skillstorm.exceptions;

import java.util.UUID;

public class FormNotFoundException extends IllegalArgumentException {

    public FormNotFoundException(String message) {
        super(message);
    }

    public FormNotFoundException(String message, UUID id) {
        this(message + " " + id);
    }
}
