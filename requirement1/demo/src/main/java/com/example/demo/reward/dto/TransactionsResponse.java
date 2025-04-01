package com.example.demo.reward.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionsResponse {
    private String userId;
    private List<TransactionDto> transactions;
}
