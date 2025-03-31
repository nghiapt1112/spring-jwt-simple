package com.example.demo.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RedeemPointsRequest {
    @NotBlank(message = "User ID is required")
    private String userId;

    @Min(value = 1, message = "Points to redeem must be at least 1")
    private int points;
}
