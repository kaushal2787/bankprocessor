# Banking Transaction Processor

A small in-memory service that manages accounts and processes deposits, withdrawals and transfers,
keeps a timestamped ledger per account, and answers balance and history queries.

Java 21, JUnit 5, no other dependencies.

```
mvn test            # standard
./run-tests.sh      # fallback: needs only a JDK and the JUnit console jar
```

## Understanding the problem

| Requirement | Interpretation I settled on |
|---|---|
| Accounts with unique IDs and balances | `AccountId` is a validated value object; ids are unique, duplicates are rejected. |
| Deposit / withdraw / transfer | Three methods on `BankingService`. Each is all-or-nothing: validate first, mutate last. |
| Prevent overdrafts and invalid amounts | Zero, negative and missing amounts raise `InvalidAmountException`. Overdrafts raise `InsufficientFundsException` (carrying balance and requested amount). Withdrawing exactly the balance is allowed. |
| Ledger per account with timestamps | Immutable `LedgerEntry` per account line: type, amount, **running balance**, timestamp, counterparty. |
| Query balances and history | `balanceOf` and `historyOf` (chronological, an immutable snapshot). Unknown accounts throw rather than look empty. |

### Edge cases I considered deliberately
- **Money is `BigDecimal`, never `double`**, fixed at 2 decimals. `0.001` is *rejected*, not silently rounded: in a money system a hidden rounding is a bug nobody sees.
- **Opening balance is recorded as a DEPOSIT** so the ledger always explains the balance (invariant: balance = sum of ledger). A failed open leaves no half-created account.
- **Transfers**: same-account transfers are rejected; unknown source/destination fail *before* anything is debited; a refused transfer changes neither account. Both ledger lines share one `transactionId` and timestamp.
- **Concurrency**: per-account locking, and transfers lock both accounts in id order so A to B and B to A cannot deadlock. Two stress tests (opposing transfers; 500 racing withdrawals on 100) prove money is conserved, balances never go negative, and ledgers reconcile line by line.
- **Time is injected** (`Clock`), so timestamp assertions are exact and tests never sleep.

## Design

```
BankingService   public API, validation, locking, orchestration
  Account        package-private; owns balance + ledger, the only code that mutates them
  LedgerEntry    immutable record of one ledger line
  Money          immutable BigDecimal wrapper (2dp)
  AccountId      validated identifier (Comparable: gives the lock order)
  BankingException hierarchy: InvalidAmount, InsufficientFunds, AccountNotFound, DuplicateAccount, InvalidTransfer
```

Why this shape: the rules that need to stay true (no negative balance, ledger matches balance) live in
`Account`, which cannot be bypassed because it is package-private. `BankingService` is the single door in.

## How I worked
Git history is the narrative: each behaviour is a **RED commit** (failing tests that document the contract)
followed by a **GREEN commit**. Test names are written as sentences so the suite reads as the spec.

## Trade-offs and what I'd do with more time
- **Locking via `synchronized` on `Account`**: simple and correct for one JVM. It does not scale across
  nodes; a real system would use DB transactions (`SELECT ... FOR UPDATE` in a consistent order) or a
  single-writer-per-account model (e.g. partitioned log).
- **Single currency, implicit.** Multi-currency needs `Money` to carry a currency and a conversion policy.
- **Idempotency**: no client-supplied request id, so a retried deposit is applied twice. I would add an
  idempotency key per operation; it matters more than almost anything else in real payments.
- **No persistence, no REST layer.** `BankingService` is deliberately the seam where a repository and an
  HTTP adapter would attach. I chose depth on the domain rules over breadth.
- **History is unbounded and returned whole**: pagination / date-range filtering would come next.
- **No overdraft limits / fees / daily limits**: would be a policy object consulted by `Account.debit`.
- **Maven**: `pom.xml` was written but I could only run the suite through `run-tests.sh` (JUnit console) in
  my environment; it is the same JUnit 5 tests either way.
