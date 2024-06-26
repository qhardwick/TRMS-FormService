package com.skillstorm.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import reactor.core.publisher.Mono;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Handle requests for resources that do not exist, attempts to submit for events less than a week away, and unsupported file types in attachments:
    @ExceptionHandler({FormNotFoundException.class, InsufficientNoticeException.class, UnsupportedFileTypeException.class})
    public Mono<ResponseEntity<ErrorMessage>> handleNotFoundExceptions(IllegalArgumentException e) {
        ErrorMessage error = new ErrorMessage();
        error.setCode(HttpStatus.NOT_FOUND.value());
        error.setMessage(e.getMessage());

        return Mono.just(ResponseEntity.status(HttpStatus.NOT_FOUND).body(error));
    }
}
