package com.skillstorm.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ReimbursementMessageDto {

    private String username;
    private BigDecimal reimbursement;
}
