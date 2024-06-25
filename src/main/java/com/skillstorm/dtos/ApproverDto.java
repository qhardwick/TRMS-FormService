package com.skillstorm.dtos;

import lombok.Data;

import java.io.Serializable;

@Data
public class ApproverDto implements Serializable {
    private String username;
    private String role;
}
