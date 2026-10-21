package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Test suite of the class: IdGenerator")
class IdGeneratorTest {

    @Test
    @DisplayName("Should generate valid identifiers for user, account and transaction")
    void givenIdGenerator_whenNextIds_thenShouldMatchExpectedPatterns() {
        //given
        //when
        String userId = IdGenerator.nextUserId();
        String txId = IdGenerator.nextTransactionId();
        String accountNumber = IdGenerator.nextAccountNumber();

        //then
        assertTrue(userId.matches("^usr-[A-Za-z0-9]+$"));
        assertTrue(txId.matches("^tan-[A-Za-z0-9]+$"));
        assertTrue(accountNumber.matches("^01\\d{6}$"));
    }
}
