package com.loyalty.wallet.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Digits;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class EarnRequest {
    @DecimalMin(value = "0.0", inclusive = true, message = "Transaction amount must be non-negative")
    @Digits(integer = 10, fraction = 2, message = "Transaction amount must have at most 10 digits and 2 decimal places")
    private BigDecimal transactionAmount;
}
