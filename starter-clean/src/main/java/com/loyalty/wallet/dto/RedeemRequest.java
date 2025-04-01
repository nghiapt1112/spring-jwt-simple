package com.loyalty.wallet.dto;

import jakarta.validation.constraints.Min;
import lombok.Data;

@Data
public class RedeemRequest {
    @Min(value = 1, message = "Points to redeem must be positive")
    private int points;
}
