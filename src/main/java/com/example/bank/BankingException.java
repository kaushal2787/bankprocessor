package com.example.bank;

/** Base type so callers can catch every business-rule failure in one place. */
public abstract class BankingException extends RuntimeException {
    protected BankingException(String message) {
        super(message);
    }
}
