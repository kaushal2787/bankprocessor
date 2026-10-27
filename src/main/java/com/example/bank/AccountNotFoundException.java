package com.example.bank;

/** Account does not exist. */
public class AccountNotFoundException extends BankingException {
    public AccountNotFoundException(String message) {
        super(message);
    }
}
