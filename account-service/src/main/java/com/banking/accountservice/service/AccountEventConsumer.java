package com.banking.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Map;

@Service
public class AccountEventConsumer {
    private final AccountService accountService;
    private static final Logger log = LoggerFactory.getLogger(AccountEventConsumer.class);

    AccountEventConsumer(AccountService accountService) {
        this.accountService = accountService;
    }

    /*
    * consumes transaction.completed event from kafka
    * credits receiver account
    * **/
    @KafkaListener(topics = "transaction.completed")
    public void consumeTransactionCompleted(@Payload Map<String, Object> payload) {
        try {
            String receiverAccount = payload.get("receiverAccountNumber").toString();
            BigDecimal amount = new BigDecimal(payload.get("amount").toString());

            log.info("Crediting account: {} amount: {}", receiverAccount, amount);

            accountService.creditBalance(receiverAccount, amount);
        }catch (Exception e){
            log.error("Error while credit account: {}", e.getMessage());
        }
    }

    /*
    * Consume fraud.detected event from kafka
    * Blocks the flagged account
    * **/
    @KafkaListener(topics = "fraud.detected")
    public void consumeFraudDetected(@Payload Map<String, Object> payload) {
        try{
            String receiverAccount = payload.get("receiverAccountNumber").toString();
            log.error("Fraud detected- blocking account: {}", receiverAccount);

            accountService.blockAccount(receiverAccount);
        } catch (Exception e) {
            log.error("Error while blocking account: {}", e.getMessage());
        }
    }
}
