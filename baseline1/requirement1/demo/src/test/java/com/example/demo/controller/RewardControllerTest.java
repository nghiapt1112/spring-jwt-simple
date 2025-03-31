//package com.example.demo.controller;
//
//import com.example.demo.config.InsufficientPointsException;
//import com.example.demo.service.RewardService;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.security.test.context.support.WithMockUser;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.util.Map;
//
//import static org.mockito.ArgumentMatchers.anyDouble;
//import static org.mockito.ArgumentMatchers.anyInt;
//import static org.mockito.ArgumentMatchers.anyString;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@SpringBootTest
//@AutoConfigureMockMvc
//class RewardControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @MockBean
//    private RewardService rewardService;
//
//    private final String testUserId = "testUser";
//    private final double testAmount = 100.0;
//    private final int testPoints = 500;
//
//    @BeforeEach
//    void setUp() {
//        // Setup common mock behaviors
//        when(rewardService.userExists(anyString())).thenReturn(true);
//        when(rewardService.earnPoints(anyString(), anyDouble())).thenReturn(1000);
//        when(rewardService.getBalance(anyString())).thenReturn(1500);
//    }
//
//    @Test
//    @WithMockUser
//    void testEarnPoints() throws Exception {
//        mockMvc.perform(post("/rewards/earn")
//                .param("userId", testUserId)
//                .param("transactionAmount", String.valueOf(testAmount))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.pointsEarned").value(1000))
//                .andExpect(jsonPath("$.newBalance").value(1500));
//    }
//
//    @Test
//    @WithMockUser
//    void testRedeemPoints() throws Exception {
//        when(rewardService.redeemPoints(anyString(), anyInt())).thenReturn(true);
//
//        mockMvc.perform(post("/rewards/redeem")
//                .param("userId", testUserId)
//                .param("points", String.valueOf(testPoints))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.pointsRedeemed").value(testPoints))
//                .andExpect(jsonPath("$.newBalance").value(1500));
//    }
//
//    @Test
//    @WithMockUser
//    void testRedeemPointsInsufficientBalance() throws Exception {
//        when(rewardService.redeemPoints(anyString(), anyInt()))
//                .thenThrow(new InsufficientPointsException("Insufficient points"));
//
//        mockMvc.perform(post("/rewards/redeem")
//                .param("userId", testUserId)
//                .param("points", String.valueOf(2000))
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.status").value("error"));
//    }
//
//    @Test
//    @WithMockUser
//    void testGetBalance() throws Exception {
//        mockMvc.perform(get("/rewards/balance")
//                .param("userId", testUserId)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.status").value("success"))
//                .andExpect(jsonPath("$.userId").value(testUserId))
//                .andExpect(jsonPath("$.balance").value(1500));
//    }
//
//    @Test
//    void testUnauthorizedAccess() throws Exception {
//        // No @WithMockUser annotation, so this should be unauthorized
//        mockMvc.perform(get("/rewards/balance")
//                .param("userId", testUserId)
//                .contentType(MediaType.APPLICATION_JSON))
//                .andExpect(status().isUnauthorized());
//    }
//}
