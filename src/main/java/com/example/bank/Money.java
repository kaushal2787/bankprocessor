package com.example.bank;

import java.math.BigDecimal;

/**
 * An immutable monetary amount in a single, implicit currency, held to two decimal places.
 * BigDecimal (never double) so that 0.1 + 0.2 == 0.3 and no cents are lost.
 */
public final class Money implements Comparable<Money> {

    public static final Money ZERO = new Money(BigDecimal.ZERO.setScale(2));

    private final BigDecimal value;

    private Money(BigDecimal value) {
        this.value = value;
    }

    public static Money of(String amount) {
        if (amount == null) {
            throw new IllegalArgumentException("Amount must not be null");
        }
        try {
            return new Money(new BigDecimal(amount.trim()).setScale(2, java.math.RoundingMode.UNNECESSARY));
        } catch (NumberFormatException | ArithmeticException e) {
            // Fail loudly: silently rounding 0.001 would hide a caller bug in a money system.
            throw new IllegalArgumentException("Not a valid amount with at most 2 decimals: " + amount, e);
        }
    }

    public Money plus(Money other) {
        return new Money(value.add(other.value));
    }

    public Money minus(Money other) {
        return new Money(value.subtract(other.value));
    }

    public boolean isPositive() {
        return value.signum() > 0;
    }

    public boolean isGreaterThan(Money other) {
        return value.compareTo(other.value) > 0;
    }

    @Override
    public int compareTo(Money other) {
        return value.compareTo(other.value);
    }

    @Override
    public boolean equals(Object o) {
        return o instanceof Money m && value.equals(m.value);
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }

    @Override
    public String toString() {
        return value.toPlainString();
    }
}
