package com.example.bank;

/** Amount must be a positive value. */
public class InvalidAmountException extends BankingException {
    public InvalidAmountException(String message) {
        super(message);
    }
}
