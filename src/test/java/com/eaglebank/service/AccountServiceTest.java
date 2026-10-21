package com.eaglebank.service;

import com.eaglebank.domain.AccountType;
import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.Currency;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.web.dto.BankAccountResponse;
import com.eaglebank.web.dto.BankAccountResponse.ListBankAccountsResponse;
import com.eaglebank.web.dto.CreateBankAccountRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Test suite of the class: AccountService")
class AccountServiceTest {

    private static final Instant FIXED_NOW = Instant.parse("2026-10-21T12:00:00Z");

    private AccountService service;

    @Mock
    private BankAccountRepository accountRepository;

    @BeforeEach
    void setup() {
        service = new AccountService(accountRepository, Clock.fixed(FIXED_NOW, ZoneOffset.UTC));
    }


    @Test
    @DisplayName("Should create personal bank account initialized with 0.00 GBP")
    void givenCreateBankAccountRequest_whenCreateAccount_thenShouldSaveAndReturnAccount() {
        //given
        String userId = "usr-123";
        CreateBankAccountRequest request = new CreateBankAccountRequest("Personal Current Account", AccountType.PERSONAL);

        //when
        BankAccountResponse response = service.createAccount(userId, request);

        //then
        assertNotNull(response);
        assertTrue(response.accountNumber().matches("^01\\d{6}$"));
        assertEquals("10-10-10", response.sortCode());
        assertEquals("Personal Current Account", response.name());
        assertEquals(AccountType.PERSONAL, response.accountType());
        assertEquals(new BigDecimal("0.00"), response.balance());
        assertEquals(Currency.GBP, response.currency());
        assertEquals(FIXED_NOW, response.createdTimestamp());

        verify(accountRepository).save(any(BankAccount.class));
    }

    @Test
    @DisplayName("Should list all bank accounts belonging to authenticated user")
    void givenAuthenticatedUserId_whenListAccounts_thenShouldReturnUserAccounts() {
        //given
        String userId = "usr-123";
        mockRepositoryForUser(userId);

        //when
        ListBankAccountsResponse result = service.listAccounts(userId);

        //then
        assertNotNull(result);
        assertEquals(1, result.accounts().size());
        assertEquals("01123456", result.accounts().getFirst().accountNumber());

        verify(accountRepository).findAllByUserIdOrderByCreatedTimestampAsc(eq(userId));
    }

    @Test
    @DisplayName("Should fetch account by account number when owned by user")
    void givenAccountNumberAndOwner_whenFetchAccount_thenShouldReturnAccount() {
        //given
        String accountNumber = "01123456";
        String ownerId = "usr-owner";
        when(accountRepository.findById(eq(accountNumber))).thenReturn(Optional.of(dummyAccount(accountNumber, ownerId)));

        //when
        BankAccountResponse response = service.fetchAccountByAccountNumber(accountNumber, ownerId);

        //then
        assertNotNull(response);
        assertEquals(accountNumber, response.accountNumber());
        verify(accountRepository).findById(eq(accountNumber));
    }

    private void mockRepositoryForUser(String userId) {
        when(accountRepository.findAllByUserIdOrderByCreatedTimestampAsc(eq(userId)))
                .thenReturn(List.of(dummyAccount("01123456", userId)));
    }

    private BankAccount dummyAccount(String accountNumber, String userId) {
        return new BankAccount(
                accountNumber, "10-10-10", userId, "Personal Account",
                AccountType.PERSONAL, new BigDecimal("50.00"), Currency.GBP, FIXED_NOW, FIXED_NOW
        );
    }
}
