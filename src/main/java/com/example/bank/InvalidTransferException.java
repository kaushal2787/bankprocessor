package com.example.bank;

/** Transfer is not allowed. */
public class InvalidTransferException extends BankingException {
    public InvalidTransferException(String message) {
        super(message);
    }
}
