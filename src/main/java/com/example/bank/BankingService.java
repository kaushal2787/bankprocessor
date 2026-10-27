package com.example.bank;

import java.time.Clock;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Entry point of the processor: owns the accounts and enforces the business rules.
 * Every operation is all-or-nothing: validate first, mutate last.
 */
public class BankingService {

    private final Map<AccountId, Account> accounts = new ConcurrentHashMap<>();
    private final Clock clock;

    public BankingService(Clock clock) {
        this.clock = clock;
    }

    public BankingService() {
        this(Clock.systemUTC());
    }

    public void openAccount(AccountId id, Money initialBalance) {
        if (initialBalance == null || initialBalance.isNegative()) {
            throw new InvalidAmountException("Opening balance must not be negative: " + initialBalance);
        }
        Account account = new Account(id);
        if (accounts.putIfAbsent(id, account) != null) {
            throw new DuplicateAccountException("Account already exists: " + id);
        }
        if (initialBalance.isPositive()) {
            // Recorded as a deposit so the ledger always explains the balance.
            account.credit(initialBalance, TransactionType.DEPOSIT, UUID.randomUUID(), clock.instant(), null);
        }
    }

    public void deposit(AccountId id, Money amount) {
        requirePositive(amount);
        Account account = find(id);
        synchronized (account) {
            account.credit(amount, TransactionType.DEPOSIT, UUID.randomUUID(), clock.instant(), null);
        }
    }

    public void withdraw(AccountId id, Money amount) {
        requirePositive(amount);
        Account account = find(id);
        synchronized (account) {
            account.debit(amount, TransactionType.WITHDRAWAL, UUID.randomUUID(), clock.instant(), null);
        }
    }

    public Money balanceOf(AccountId id) {
        Account account = find(id);
        synchronized (account) {
            return account.balance();
        }
    }

    public List<LedgerEntry> historyOf(AccountId id) {
        Account account = find(id);
        synchronized (account) {
            return account.history();
        }
    }

    private Account find(AccountId id) {
        Account account = id == null ? null : accounts.get(id);
        if (account == null) {
            throw new AccountNotFoundException("Account does not exist: " + id);
        }
        return account;
    }

    private static void requirePositive(Money amount) {
        if (amount == null || !amount.isPositive()) {
            throw new InvalidAmountException("Amount must be greater than zero: " + amount);
        }
    }
}
