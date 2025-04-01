package com.loyalty.wallet.controller;

import com.loyalty.wallet.dto.EarnRequest;
import com.loyalty.wallet.dto.RedeemRequest;
import com.loyalty.wallet.model.UserRewards;
import com.loyalty.wallet.service.RewardsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/rewards")
@Validated
public class RewardsController {

    private final RewardsService rewardsService;

    @Autowired
    public RewardsController(RewardsService rewardsService) {
        this.rewardsService = rewardsService;
    }

    @GetMapping("/balance")
    public UserRewards getBalance(Authentication authentication) {
        String userId = authentication.getName();
        return rewardsService.getBalance(userId);
    }

    @PostMapping("/earn")
    public UserRewards earnPoints(
            @Valid @RequestBody EarnRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        return rewardsService.earnPoints(userId, request.getTransactionAmount());
    }

    @PostMapping("/redeem")
    public UserRewards redeemPoints(
            @Valid @RequestBody RedeemRequest request,
            Authentication authentication) {
        
        String userId = authentication.getName();
        UserRewards userRewards = rewardsService.redeemPoints(userId, request.getPoints());
        
        if (userRewards == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Insufficient points balance");
        }
        
        return userRewards;
    }
}
