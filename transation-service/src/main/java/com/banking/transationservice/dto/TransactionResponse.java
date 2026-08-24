package com.banking.transationservice.dto;

import com.banking.transationservice.constants.TransactionType;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private String id;
    private String senderAccountNumber;
    private String receiverAccountNumber;
    private BigDecimal amount;
    private TransactionType transactionType;
    private TransactionType transactionStatus;
    private String description;
    private String failureReason;
    private String referenceNumber;
    private LocalDate createdAt;
    private LocalDate completedAt;

    public void setId(String id) {
        this.id = id;
    }

    public void setSenderAccountNumber(String senderAccountNumber) {
        this.senderAccountNumber = senderAccountNumber;
    }

    public void setReceiverAccountNumber(String receiverAccountNumber) {
        this.receiverAccountNumber = receiverAccountNumber;
    }

    public void setAmount(BigDecimal amount) {
        this.amount = amount;
    }

    public void setTransactionType(TransactionType transactionType) {
        this.transactionType = transactionType;
    }

    public void setTransactionStatus(TransactionType transactionStatus) {
        this.transactionStatus = transactionStatus;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setFailureReason(String failureReason) {
        this.failureReason = failureReason;
    }

    public void setReferenceNumber(String referenceNumber) {
        this.referenceNumber = referenceNumber;
    }

    public void setCreatedAt(LocalDate createdAt) {
        this.createdAt = createdAt;
    }

    public void setCompletedAt(LocalDate completedAt) {
        this.completedAt = completedAt;
    }
}
