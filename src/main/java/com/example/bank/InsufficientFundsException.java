package com.example.bank;

/** Raised instead of letting a balance go negative. Carries the numbers so callers can explain the refusal. */
public class InsufficientFundsException extends BankingException {
    private final AccountId accountId;
    private final Money balance;
    private final Money requested;

    public InsufficientFundsException(AccountId accountId, Money balance, Money requested) {
        super("Insufficient funds in " + accountId + ": balance " + balance + ", requested " + requested);
        this.accountId = accountId;
        this.balance = balance;
        this.requested = requested;
    }

    public AccountId accountId() { return accountId; }
    public Money balance() { return balance; }
    public Money requested() { return requested; }
}
