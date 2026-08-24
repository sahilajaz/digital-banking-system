package com.banking.transationservice.controller;

import com.banking.transationservice.service.TransactionService;

public class TransactionController {
    private final TransactionService transactionService;

    TransactionController(TransactionService transactionService) {
        this.transactionService = transactionService;
    }


}
