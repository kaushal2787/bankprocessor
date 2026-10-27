package com.example.bank;

import java.time.Instant;
import java.util.UUID;

/**
 * One immutable line in an account's ledger.
 *
 * @param transactionId groups the two sides of a transfer; unique per business operation
 * @param balanceAfter  running balance, so a statement can be audited line by line
 * @param counterparty  the other account for transfers; {@code null} for deposits and withdrawals
 */
public record LedgerEntry(
        UUID transactionId,
        AccountId accountId,
        TransactionType type,
        Money amount,
        Money balanceAfter,
        Instant timestamp,
        AccountId counterparty) {
}
