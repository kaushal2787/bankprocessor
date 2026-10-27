package com.example.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Money")
class MoneyTest {

    @Test
    void equalAmountsAreEqualRegardlessOfTrailingZeros() {
        assertEquals(Money.of("10"), Money.of("10.00"));
        assertEquals(Money.of("10").hashCode(), Money.of("10.00").hashCode());
    }

    @Test
    void addsAndSubtracts() {
        assertEquals(Money.of("12.50"), Money.of("10.00").plus(Money.of("2.50")));
        assertEquals(Money.of("7.50"), Money.of("10.00").minus(Money.of("2.50")));
    }

    @Test
    void comparesAmounts() {
        assertTrue(Money.of("10.01").isGreaterThan(Money.of("10.00")));
        assertFalse(Money.of("10.00").isGreaterThan(Money.of("10.00")));
    }

    @Test
    void knowsWhetherItIsStrictlyPositive() {
        assertTrue(Money.of("0.01").isPositive());
        assertFalse(Money.ZERO.isPositive());
        assertFalse(Money.of("-1").isPositive());
    }

    @Test
    void rejectsSubCentPrecisionInsteadOfSilentlyRounding() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("0.001"));
    }

    @Test
    void rejectsNonNumericInput() {
        assertThrows(IllegalArgumentException.class, () -> Money.of("ten"));
        assertThrows(IllegalArgumentException.class, () -> Money.of(null));
    }
}
