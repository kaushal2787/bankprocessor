package com.example.bank;

/** Unique account identifier. Comparable so that multi-account operations can lock in a stable order. */
public record AccountId(String value) implements Comparable<AccountId> {

    public AccountId {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Account id must not be blank");
        }
        value = value.trim();
    }

    public static AccountId of(String value) {
        return new AccountId(value);
    }

    @Override
    public int compareTo(AccountId other) {
        return value.compareTo(other.value);
    }

    @Override
    public String toString() {
        return value;
    }
}
