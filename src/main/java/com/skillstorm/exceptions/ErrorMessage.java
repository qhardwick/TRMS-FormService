package com.skillstorm.exceptions;

import lombok.Data;

@Data
public class ErrorMessage {

    private int code;
    private String message;
}
