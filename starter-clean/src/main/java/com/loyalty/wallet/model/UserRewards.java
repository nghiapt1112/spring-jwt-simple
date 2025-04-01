package com.loyalty.wallet.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserRewards {
    private String userId;
    private int points;
}
