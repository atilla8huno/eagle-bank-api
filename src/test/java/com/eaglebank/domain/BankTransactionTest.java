package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test suite of the class: BankTransaction")
class BankTransactionTest {

    @Test
    @DisplayName("Should instantiate BankTransaction and IdempotencyRecord entities")
    void givenTransactionParameters_whenNew_thenShouldMaintainImmutableState() {
        //given
        Instant t1 = Instant.parse("2026-10-20T11:00:00Z");

        //when
        BankTransaction tx = new BankTransaction(
                "tan-xyz98765",
                "01123456",
                "usr-abc12345",
                new BigDecimal("50.00"),
                Currency.GBP,
                TransactionType.DEPOSIT,
                "Bonus",
                t1
        );
        IdempotencyRecord idem = new IdempotencyRecord("01123456", "key-1", "tan-xyz98765", t1);

        //then
        assertEquals("tan-xyz98765", tx.getId());
        assertEquals("01123456", tx.getAccountNumber());
        assertEquals("usr-abc12345", tx.getUserId());
        assertEquals(new BigDecimal("50.00"), tx.getAmount());
        assertEquals(Currency.GBP, tx.getCurrency());
        assertEquals(TransactionType.DEPOSIT, tx.getType());
        assertEquals("Bonus", tx.getReference());
        assertEquals(t1, tx.getCreatedTimestamp());

        assertNull(idem.getId());
        assertEquals("01123456", idem.getAccountNumber());
        assertEquals("key-1", idem.getIdempotencyKey());
        assertEquals("tan-xyz98765", idem.getTransactionId());
        assertEquals(t1, idem.getCreatedTimestamp());
    }

    @Test
    @DisplayName("Should parse valid TransactionType enum and reject invalid value")
    void givenTransactionTypeString_whenFromValue_thenShouldParseOrThrow() {
        //given
        Executable invalidTxType = () -> TransactionType.fromValue("transfer");

        //when & then
        assertEquals(TransactionType.DEPOSIT, TransactionType.fromValue("deposit"));
        assertEquals("deposit", TransactionType.DEPOSIT.getValue());
        assertEquals(TransactionType.WITHDRAWAL, TransactionType.fromValue("WITHDRAWAL"));
        assertEquals("withdrawal", TransactionType.WITHDRAWAL.getValue());
        assertThrows(IllegalArgumentException.class, invalidTxType);
    }
}
