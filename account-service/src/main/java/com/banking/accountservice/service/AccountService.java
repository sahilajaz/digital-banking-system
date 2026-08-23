package com.banking.accountservice.service;

import com.banking.accountservice.constants.AccountStatus;
import com.banking.accountservice.constants.AccountType;
import com.banking.accountservice.dto.AccountResponse;
import com.banking.accountservice.dto.CreateAccountRequest;
import com.banking.accountservice.model.Account;
import com.banking.accountservice.repository.AccountRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.security.SecureRandom;

@Service
public class AccountService {
    private final AccountRepository accountRepository;
    private static final SecureRandom random = new SecureRandom();
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    AccountService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    public AccountResponse createAccount(CreateAccountRequest req) {
        log.info("Creating account for: {}", req.getEmail());

         if (accountRepository.existsByEmail(req.getEmail())) {
            throw new RuntimeException("Account already exits for the email: " + req.getEmail());
         }

         Account account = new Account();
         account.setAccountHolderName(req.getAccountHolderName());
         account.setEmail(req.getEmail());
         account.setPhone(req.getPhone());
         account.setAccountType(req.getAccountType());
         account.setStatus(AccountStatus.ACTIVE);
         account.setBalance(req.getInitialBalance());
         account.setAccountNumber(generateAccountNumber());
         account.setDailyTransactionLimit(
                 req.getAccountType() == AccountType.SAVINGS
                 ? new BigDecimal("100000") : new BigDecimal("500000")
         );

        try {
            Account savedAccount = accountRepository.save(account);
            log.info("Saved account for: {}", savedAccount.getAccountNumber());
            return mapToResponse(savedAccount);
        } catch (Exception e) {
            throw new RuntimeException("Unable to save account for: " + req.getEmail(), e);
        }
    }

    public AccountResponse getAccount(String accountNumber) {
        try {
            log.info("Getting account for: {}", accountNumber);
            Account account = accountRepository.findByAccountNumber(accountNumber)
                    .orElseThrow(() -> new RuntimeException(
                            "Account not found for: " + accountNumber
                    ));
            log.info("Found account for: {}", account.getAccountNumber());
            return mapToResponse(account);
        } catch (RuntimeException e) {
            throw new RuntimeException("Account not found for: " + accountNumber, e);
        }
    }

    public BigDecimal getBalance(String accountNumber) {
        log.info("Getting balance for: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found for: " + accountNumber
                ));
        log.info("Found account for account number: {}", account.getAccountNumber());
        return account.getBalance();
    }

    /*
    *Block account - called by fraud detection service via kafka
    * **/
    public void blockAccount(String accountNumber) {
        log.info("Blocking account number: {}", accountNumber);
        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found for: " + accountNumber
                ));

        account.setStatus(AccountStatus.BLOCKED);
        accountRepository.save(account);
        log.info("Blocked account number: {}", account.getAccountNumber());
    }

    /*
    * Deduct balance from sender account
    * Called by transaction service
    * **/
    public void deductBalance(String accountNumber, BigDecimal amount) {
        log.info("Deducting balance for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found for: " + accountNumber
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account not active: " + accountNumber);
        }

        if (account.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds for account: " + account.getBalance());
        }

        account.setBalance(account.getBalance().subtract(amount));
        accountRepository.save(account);
        log.info("Balance updated. New balance is: {}", account.getBalance());
    }

    /*
    * Also called by Transaction service via kafka
    * **/
    public void creditBalance(String accountNumber, BigDecimal amount) {
        log.info("Crediting balance for account: {}", accountNumber);

        Account account = accountRepository.findByAccountNumber(accountNumber)
                .orElseThrow(() -> new RuntimeException(
                        "Account not found for: " + accountNumber
                ));

        if (account.getStatus() != AccountStatus.ACTIVE) {
            throw new RuntimeException("Account not active: " + accountNumber);
        }

        account.setBalance(account.getBalance().add(amount));
        accountRepository.save(account);
        log.info("Balance credited. New balance is: {}", account.getBalance());
    }

    private String generateAccountNumber() {
        String accountNumber;
        do {
            long number = random.nextLong(1_000_000_000_000L);
            accountNumber = String.format("%012d", number);
        } while (accountRepository.existsByAccountNumber(accountNumber));

        return  accountNumber;
    }

    private AccountResponse mapToResponse(Account account) {
        AccountResponse accountResponse = new AccountResponse();
        accountResponse.setId(account.getId());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setAccountHolderName(account.getAccountHolderName());
        accountResponse.setStatus(account.getStatus());
        accountResponse.setEmail(account.getEmail());
        accountResponse.setPhone(account.getPhone());
        accountResponse.setAccountType(account.getAccountType());
        accountResponse.setBalance(account.getBalance());
        accountResponse.setAccountNumber(account.getAccountNumber());
        accountResponse.setDailyTransactionLimit(account.getDailyTransactionLimit());
        accountResponse.setCreatedAt(account.getCreatedAt());

        return accountResponse;
    }
}
