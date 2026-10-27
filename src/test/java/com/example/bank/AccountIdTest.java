package com.example.bank;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;

class AccountIdTest {
    @Test
    void rejectsBlankAndNullIds() {
        assertThrows(IllegalArgumentException.class, () -> AccountId.of(" "));
        assertThrows(IllegalArgumentException.class, () -> AccountId.of(""));
        assertThrows(IllegalArgumentException.class, () -> AccountId.of(null));
    }

    @Test
    void surroundingWhitespaceDoesNotCreateADifferentAccount() {
        assertEquals(AccountId.of("alice"), AccountId.of("  alice "));
    }
}
