package com.example.bank;

import static org.junit.jupiter.api.Assertions.*;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

@DisplayName("BankingService")
class BankingServiceTest {

    static final AccountId ALICE = AccountId.of("alice");
    static final AccountId BOB = AccountId.of("bob");

    MutableClock clock;
    BankingService bank;

    @BeforeEach
    void setUp() {
        clock = new MutableClock();
        bank = new BankingService(clock);
    }

    @Nested
    @DisplayName("opening accounts")
    class OpeningAccounts {

        @Test
        void newAccountHasItsInitialBalance() {
            bank.openAccount(ALICE, Money.of("100.00"));
            assertEquals(Money.of("100.00"), bank.balanceOf(ALICE));
        }

        @Test
        void accountCanBeOpenedEmpty() {
            bank.openAccount(ALICE, Money.ZERO);
            assertEquals(Money.ZERO, bank.balanceOf(ALICE));
            assertTrue(bank.historyOf(ALICE).isEmpty());
        }

        @Test
        void nonZeroOpeningBalanceIsRecordedAsADepositSoTheLedgerAlwaysReconcilesWithTheBalance() {
            bank.openAccount(ALICE, Money.of("100.00"));
            List<LedgerEntry> history = bank.historyOf(ALICE);
            assertEquals(1, history.size());
            assertEquals(TransactionType.DEPOSIT, history.get(0).type());
            assertEquals(Money.of("100.00"), history.get(0).balanceAfter());
        }

        @Test
        void idsAreUnique() {
            bank.openAccount(ALICE, Money.ZERO);
            assertThrows(DuplicateAccountException.class, () -> bank.openAccount(ALICE, Money.of("5")));
        }

        @Test
        void openingBalanceCannotBeNegative() {
            assertThrows(InvalidAmountException.class, () -> bank.openAccount(ALICE, Money.of("-0.01")));
        }

        @Test
        void aFailedOpenDoesNotLeaveAnAccountBehind() {
            assertThrows(InvalidAmountException.class, () -> bank.openAccount(ALICE, Money.of("-1")));
            assertThrows(AccountNotFoundException.class, () -> bank.balanceOf(ALICE));
        }
    }

    @Nested
    @DisplayName("deposits")
    class Deposits {

        @BeforeEach
        void openAlice() {
            bank.openAccount(ALICE, Money.of("100.00"));
        }

        @Test
        void increaseTheBalance() {
            bank.deposit(ALICE, Money.of("25.50"));
            assertEquals(Money.of("125.50"), bank.balanceOf(ALICE));
        }

        @Test
        void areRecordedInTheLedgerWithTimestampAndRunningBalance() {
            clock.advance(Duration.ofMinutes(5));
            bank.deposit(ALICE, Money.of("25.50"));

            LedgerEntry entry = bank.historyOf(ALICE).get(1);
            assertEquals(TransactionType.DEPOSIT, entry.type());
            assertEquals(Money.of("25.50"), entry.amount());
            assertEquals(Money.of("125.50"), entry.balanceAfter());
            assertEquals(Instant.parse("2026-01-01T09:05:00Z"), entry.timestamp());
            assertEquals(ALICE, entry.accountId());
            assertNull(entry.counterparty());
        }

        @Test
        void rejectZeroNegativeAndMissingAmountsWithoutChangingAnything() {
            assertThrows(InvalidAmountException.class, () -> bank.deposit(ALICE, Money.ZERO));
            assertThrows(InvalidAmountException.class, () -> bank.deposit(ALICE, Money.of("-5")));
            assertThrows(InvalidAmountException.class, () -> bank.deposit(ALICE, null));
            assertEquals(Money.of("100.00"), bank.balanceOf(ALICE));
            assertEquals(1, bank.historyOf(ALICE).size());
        }

        @Test
        void toAnUnknownAccountFail() {
            assertThrows(AccountNotFoundException.class, () -> bank.deposit(BOB, Money.of("1")));
        }
    }

    @Nested
    @DisplayName("queries")
    class Queries {

        @Test
        void historyIsChronologicalAndCannotBeUsedToTamperWithTheLedger() {
            bank.openAccount(ALICE, Money.ZERO);
            bank.deposit(ALICE, Money.of("1"));
            clock.advance(Duration.ofSeconds(1));
            bank.deposit(ALICE, Money.of("2"));

            List<LedgerEntry> history = bank.historyOf(ALICE);
            assertEquals(List.of(Money.of("1"), Money.of("2")), history.stream().map(LedgerEntry::amount).toList());
            assertThrows(UnsupportedOperationException.class, () -> history.clear());
        }

        @Test
        void historyIsASnapshotNotALiveView() {
            bank.openAccount(ALICE, Money.ZERO);
            List<LedgerEntry> before = bank.historyOf(ALICE);
            bank.deposit(ALICE, Money.of("1"));
            assertEquals(0, before.size());
        }

        @Test
        void unknownAccountsAreReportedNotSilentlyTreatedAsEmpty() {
            assertThrows(AccountNotFoundException.class, () -> bank.balanceOf(BOB));
            assertThrows(AccountNotFoundException.class, () -> bank.historyOf(BOB));
        }
    }
}
