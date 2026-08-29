package com.banking.transationservice.service;

import com.banking.transationservice.dto.TransactionResponse;
import com.banking.transationservice.dto.TransferRequest;
import com.banking.transationservice.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    private final TransactionRepository transactionRepository;

    TransactionService(TransactionRepository transactionRepository) {
        this.transactionRepository = transactionRepository;
    }

    public TransactionResponse transfer(TransferRequest request) {
        return null;
    }

    public List<TransactionResponse> getTransaction(String transactionId) {
        return null;
    }

    public TransactionResponse getTransactionHistory(String accountNumber) {
        return null;
    }

    public TransactionResponse verifyOtp(String transactionId, String otp) {
        return null;
    }
}
