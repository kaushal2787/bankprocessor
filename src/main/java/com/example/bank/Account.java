package com.example.bank;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * An account owns its balance and its ledger and is the only place that mutates them,
 * so "balance == sum of ledger" cannot be broken from outside.
 *
 * Not thread-safe by itself: {@link BankingService} serialises access per account.
 */
final class Account {

    private final AccountId id;
    private Money balance = Money.ZERO;
    private final List<LedgerEntry> ledger = new ArrayList<>();

    Account(AccountId id) {
        this.id = id;
    }

    AccountId id() {
        return id;
    }

    Money balance() {
        return balance;
    }

    List<LedgerEntry> history() {
        return Collections.unmodifiableList(new ArrayList<>(ledger));
    }

    void credit(Money amount, TransactionType type, UUID transactionId, Instant at, AccountId counterparty) {
        balance = balance.plus(amount);
        record(type, amount, transactionId, at, counterparty);
    }

    private void record(TransactionType type, Money amount, UUID transactionId, Instant at, AccountId counterparty) {
        ledger.add(new LedgerEntry(transactionId, id, type, amount, balance, at, counterparty));
    }
}
