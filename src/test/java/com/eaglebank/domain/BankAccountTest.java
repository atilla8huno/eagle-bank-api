package com.eaglebank.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test suite of the class: BankAccount")
class BankAccountTest {

    @Test
    @DisplayName("Should instantiate and update BankAccount entity")
    void givenBankAccountParameters_whenNewAndUpdate_thenShouldMaintainState() {
        //given
        Instant t1 = Instant.parse("2026-10-21T10:00:00Z");
        Instant t2 = Instant.parse("2026-10-21T11:00:00Z");

        //when
        BankAccount account = new BankAccount(
                "01123456",
                BankAccount.DEFAULT_SORT_CODE,
                "usr-abc12345",
                "Main Account",
                AccountType.PERSONAL,
                new BigDecimal("100.00"),
                Currency.GBP,
                t1,
                t1
        );
        account.updateBalance(Money.gbp(new BigDecimal("250.50")), t2);
        account.updateDetails(null, null, t2);
        account.updateDetails("Savings Account", AccountType.PERSONAL, t2);

        //then
        assertEquals("01123456", account.getAccountNumber());
        assertEquals("10-10-10", account.getSortCode());
        assertEquals("usr-abc12345", account.getUserId());
        assertEquals("Savings Account", account.getName());
        assertEquals(AccountType.PERSONAL, account.getAccountType());
        assertEquals(new BigDecimal("250.50"), account.getBalance());
        assertEquals(new BigDecimal("250.50"), account.getMoneyBalance().amount());
        assertEquals(Currency.GBP, account.getCurrency());
        assertEquals(t1, account.getCreatedTimestamp());
        assertEquals(t2, account.getUpdatedTimestamp());
        assertNull(account.getVersion());
    }

    @Test
    @DisplayName("Should parse valid AccountType and Currency enums and reject invalid values")
    void givenEnumStrings_whenFromValue_thenShouldParseOrThrow() {
        //given
        Executable invalidAccountType = () -> AccountType.fromValue("corporate");
        Executable invalidCurrency = () -> Currency.fromValue("EUR");

        //when & then
        assertEquals(AccountType.PERSONAL, AccountType.fromValue("personal"));
        assertEquals("personal", AccountType.PERSONAL.getValue());
        assertEquals(Currency.GBP, Currency.fromValue("gbp"));
        assertEquals("GBP", Currency.GBP.getCode());
        assertThrows(IllegalArgumentException.class, invalidAccountType);
        assertThrows(IllegalArgumentException.class, invalidCurrency);
    }
}
