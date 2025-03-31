package com.example.demo.integration;

import com.example.demo.infrastructure.security.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class RewardIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private JwtService jwtService;

    @Autowired
    private UserDetailsService userDetailsService;

    private String jwtToken;
    private final String testUserId = "user123"; // User already initialized in the service

    @BeforeEach
    void setUp() {
        // Get JWT token for predefined user
        UserDetails userDetails = userDetailsService.loadUserByUsername("user123");
        jwtToken = jwtService.generateToken(userDetails.getUsername());
    }

    @Test
    void testFullRewardFlow() throws Exception {
        // 1. Get initial balance
        ResultActions initialBalanceResult = getBalance();
        initialBalanceResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());

        int initialBalance = Integer.parseInt(
                initialBalanceResult.andReturn().getResponse().getContentAsString()
                        .replaceAll(".*\"balance\":(\\d+).*", "$1"));

        // 2. Earn points
        earnPoints(100.0)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.pointsEarned").value(1000));

        // 3. Verify updated balance
        getBalance()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(initialBalance + 1000));

        // 4. Redeem points
        redeemPoints(300)
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("success"))
                .andExpect(jsonPath("$.pointsRedeemed").value(300));

        // 5. Verify final balance
        getBalance()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").value(initialBalance + 1000 - 300));
                
        // 6. Check transaction history
        getTransactionHistory()
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.transactions").isArray())
                .andExpect(jsonPath("$.transactions.length()").value(3)); // Initial + earn + redeem
    }

    @Test
    void testInsufficientPointsRedemption() throws Exception {
        // 1. Get initial balance
        ResultActions initialBalanceResult = getBalance();
        initialBalanceResult
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.balance").exists());

        int initialBalance = Integer.parseInt(
                initialBalanceResult.andReturn().getResponse().getContentAsString()
                        .replaceAll(".*\"balance\":(\\d+).*", "$1"));

        // 2. Try to redeem more points than available
        redeemPoints(initialBalance + 1000)
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("error"))
                .andExpect(jsonPath("$.message").value(String.format(
                        "Insufficient points to redeem: %d available, %d requested", 
                        initialBalance, initialBalance + 1000)));
    }

    private ResultActions getBalance() throws Exception {
        return mockMvc.perform(get("/rewards/balance")
                .param("userId", testUserId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions earnPoints(double amount) throws Exception {
        return mockMvc.perform(post("/rewards/earn")
                .param("userId", testUserId)
                .param("transactionAmount", String.valueOf(amount))
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON));
    }

    private ResultActions redeemPoints(int points) throws Exception {
        return mockMvc.perform(post("/rewards/redeem")
                .param("userId", testUserId)
                .param("points", String.valueOf(points))
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON));
    }
    
    private ResultActions getTransactionHistory() throws Exception {
        return mockMvc.perform(get("/rewards/transactions")
                .param("userId", testUserId)
                .header("Authorization", "Bearer " + jwtToken)
                .contentType(MediaType.APPLICATION_JSON));
    }
}
