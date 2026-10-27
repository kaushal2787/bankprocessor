package com.example.bank;

import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;

@DisplayName("BankingService under concurrent load")
class BankingServiceConcurrencyTest {

    static final AccountId A = AccountId.of("A");
    static final AccountId B = AccountId.of("B");

    @Test
    @Timeout(30)
    void opposingTransfersNeitherDeadlockNorCreateOrDestroyMoney() throws Exception {
        BankingService bank = new BankingService();
        bank.openAccount(A, Money.of("1000"));
        bank.openAccount(B, Money.of("1000"));

        int threads = 8, perThread = 2_000;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        List<Future<?>> done = new ArrayList<>();
        for (int t = 0; t < threads; t++) {
            boolean aToB = t % 2 == 0;
            done.add(pool.submit(() -> {
                start.await();
                for (int i = 0; i < perThread; i++) {
                    try {
                        if (aToB) bank.transfer(A, B, Money.of("1")); else bank.transfer(B, A, Money.of("1"));
                    } catch (InsufficientFundsException ignored) {
                        // legitimate refusal, must simply leave no trace
                    }
                }
                return null;
            }));
        }
        start.countDown();
        for (Future<?> f : done) f.get();
        pool.shutdown();

        assertEquals(Money.of("2000"), bank.balanceOf(A).plus(bank.balanceOf(B)), "money is conserved");
        assertFalse(bank.balanceOf(A).isNegative());
        assertFalse(bank.balanceOf(B).isNegative());
        assertLedgerReconciles(bank, A, Money.of("1000"));
        assertLedgerReconciles(bank, B, Money.of("1000"));
    }

    @Test
    @Timeout(30)
    void concurrentWithdrawalsCanNeverOverdrawTheAccount() throws Exception {
        BankingService bank = new BankingService();
        bank.openAccount(A, Money.of("100"));

        ExecutorService pool = Executors.newFixedThreadPool(16);
        List<Future<Boolean>> results = new ArrayList<>();
        for (int i = 0; i < 500; i++) {
            results.add(pool.submit(() -> {
                try { bank.withdraw(A, Money.of("1")); return true; }
                catch (InsufficientFundsException e) { return false; }
            }));
        }
        long succeeded = 0;
        for (Future<Boolean> f : results) if (f.get()) succeeded++;
        pool.shutdown();

        assertEquals(100, succeeded, "exactly the available funds were paid out");
        assertEquals(Money.ZERO, bank.balanceOf(A));
    }

    private static void assertLedgerReconciles(BankingService bank, AccountId id, Money expectedOpeningBalance) {
        Money running = Money.ZERO;
        for (LedgerEntry e : bank.historyOf(id)) {
            boolean credit = e.type() == TransactionType.DEPOSIT || e.type() == TransactionType.TRANSFER_IN;
            running = credit ? running.plus(e.amount()) : running.minus(e.amount());
            assertEquals(running, e.balanceAfter(), "running balance matches at every ledger line");
        }
        assertEquals(bank.balanceOf(id), running);
    }
}
