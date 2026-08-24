package com.banking.transationservice.constants;

/*
* PENDING -> PROCESSING -> COMPLETED
*                       -> PENDING_VERIFICATION (suspicious detected)
*                       -> COMPLETED (verified)
*                        -> FLAGGED (saga)
* **/
public enum TransactionStatus {
    PENDING, PROCESSING, PENDING_VERIFICATION, COMPLETED, FAILED, FLAGGED
}
