package com.banking.transationservice.controller;

import com.banking.transationservice.dto.TransactionResponse;
import com.banking.transationservice.dto.TransferRequest;
import com.banking.transationservice.service.TransactionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/transactions")
public class TransactionController {
    private final TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }

    @PostMapping("/transfer")
    public ResponseEntity<TransactionResponse> transfer(@Valid @RequestBody TransferRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                transactionService.transfer(request)
        );
    }

    @GetMapping("/{transactionId}")
    public ResponseEntity<List<TransactionResponse>> getTransaction(@PathVariable String transactionId) {
        return ResponseEntity.ok(transactionService.getTransaction(transactionId));
    }

    @GetMapping("/account/{accountNumber}")
    public ResponseEntity<TransactionResponse> getTransactionHistory(@PathVariable String accountNumber) {
        return ResponseEntity.ok(transactionService.getTransactionHistory(accountNumber));
    }

    @PostMapping("/{transactionId}/verify")
    public ResponseEntity<TransactionResponse> verifyOTP(
            @PathVariable String transactionId,
            @RequestParam String otp
    ) {
        return ResponseEntity.ok(transactionService.verifyOtp(transactionId, otp));
    }
}
