package com.example.bank;

/** Account already exists. */
public class DuplicateAccountException extends BankingException {
    public DuplicateAccountException(String message) {
        super(message);
    }
}
