//package com.digitalwallet.controller;
//
//import com.digitalwallet.exception.IdentityVerificationException;
//import com.digitalwallet.model.Transaction;
//import com.digitalwallet.model.User;
//import com.digitalwallet.model.VerificationResult;
//import com.digitalwallet.service.TransactionService;
//import com.digitalwallet.service.identity.IdentityVerificationService;
//import com.digitalwallet.service.identity.VerificationLevel;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
//import org.springframework.boot.test.mock.mockito.MockBean;
//import org.springframework.http.MediaType;
//import org.springframework.test.web.servlet.MockMvc;
//
//import java.math.BigDecimal;
//import java.time.LocalDateTime;
//
//import static org.mockito.ArgumentMatchers.any;
//import static org.mockito.Mockito.when;
//import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
//import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
//
//@WebMvcTest(TransactionController.class)
//class TransactionControllerTest {
//
//    @Autowired
//    private MockMvc mockMvc;
//
//    @Autowired
//    private ObjectMapper objectMapper;
//
//    @MockBean
//    private TransactionService transactionService;
//
//    @MockBean
//    private IdentityVerificationService identityVerificationService;
//
//    private Transaction transaction;
//    private User user;
//    private VerificationResult successfulVerification;
//    private VerificationResult failedVerification;
//
//    @BeforeEach
//    void setUp() {
//        // Set up test transaction
//        transaction = new Transaction();
//        transaction.setId("txn123");
//        transaction.setUserId("user123");
//        transaction.setAmount(new BigDecimal("500.00"));
//        transaction.setIpAddress("192.168.1.1");
//        transaction.setDeviceId("device123");
//
//        // Set up test user
//        user = new User();
//        user.setId("user123");
//        user.setFullName("Jane Doe");
//
//        // Set up verification results
//        successfulVerification = VerificationResult.builder()
//                .verified(true)
//                .requestId("req123")
//                .providerId("STANDARD")
//                .timestamp(LocalDateTime.now())
//                .message("Identity verified successfully")
//                .build();
//
//        failedVerification = VerificationResult.builder()
//                .verified(false)
//                .requestId("req123")
//                .providerId("STANDARD")
//                .timestamp(LocalDateTime.now())
//                .message("Identity verification failed")
//                .build();
//    }
//
//    @Test
//    @DisplayName("Should process transaction when verification succeeds")
//    void shouldProcessTransactionWhenVerificationSucceeds() throws Exception {
//        // Arrange
//        when(transactionService.getUserById("user123")).thenReturn(user);
//        when(identityVerificationService.verifyIdentityForTransaction(any(), any(), any()))
//                .thenReturn(successfulVerification);
//        when(transactionService.processTransaction(transaction)).thenReturn(transaction);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(transaction)))
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.id").value("txn123"));
//    }
//
//    @Test
//    @DisplayName("Should return bad request when verification fails")
//    void shouldReturnBadRequestWhenVerificationFails() throws Exception {
//        // Arrange
//        when(transactionService.getUserById("user123")).thenReturn(user);
//        when(identityVerificationService.verifyIdentityForTransaction(any(), any(), any()))
//                .thenReturn(failedVerification);
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(transaction)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$.verified").value(false))
//                .andExpect(jsonPath("$.message").value("Identity verification failed"));
//    }
//
//    @Test
//    @DisplayName("Should return bad request when verification throws exception")
//    void shouldReturnBadRequestWhenVerificationThrowsException() throws Exception {
//        // Arrange
//        when(transactionService.getUserById("user123")).thenReturn(user);
//        when(identityVerificationService.verifyIdentityForTransaction(any(), any(), any()))
//                .thenThrow(new IdentityVerificationException("API error"));
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(transaction)))
//                .andExpect(status().isBadRequest())
//                .andExpect(jsonPath("$").value("Identity verification failed: API error"));
//    }
//
//    @Test
//    @DisplayName("Should return internal server error when processing throws exception")
//    void shouldReturnInternalServerErrorWhenProcessingThrowsException() throws Exception {
//        // Arrange
//        when(transactionService.getUserById("user123")).thenReturn(user);
//        when(identityVerificationService.verifyIdentityForTransaction(any(), any(), any()))
//                .thenReturn(successfulVerification);
//        when(transactionService.processTransaction(transaction))
//                .thenThrow(new RuntimeException("Processing error"));
//
//        // Act & Assert
//        mockMvc.perform(post("/api/v1/transactions")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(transaction)))
//                .andExpect(status().isInternalServerError())
//                .andExpect(jsonPath("$").value("Error processing transaction: Processing error"));
//    }
//}