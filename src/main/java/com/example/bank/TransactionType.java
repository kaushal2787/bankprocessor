package com.example.bank;

/** What happened to an account. A transfer appears as TRANSFER_OUT on one ledger and TRANSFER_IN on the other. */
public enum TransactionType {
    DEPOSIT, WITHDRAWAL, TRANSFER_OUT, TRANSFER_IN
}
