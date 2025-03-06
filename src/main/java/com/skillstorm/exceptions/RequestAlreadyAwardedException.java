package com.skillstorm.exceptions;

public class RequestAlreadyAwardedException extends  IllegalArgumentException {
    public RequestAlreadyAwardedException(String message) {
        super(message);
    }
}
