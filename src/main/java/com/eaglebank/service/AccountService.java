package com.eaglebank.service;

import com.eaglebank.domain.BankAccount;
import com.eaglebank.domain.IdGenerator;
import com.eaglebank.domain.Money;
import com.eaglebank.domain.User;
import com.eaglebank.repository.BankAccountRepository;
import com.eaglebank.repository.UserRepository;
import com.eaglebank.service.specification.AccountSpecifications;
import com.eaglebank.web.dto.BankAccountResponse;
import com.eaglebank.web.dto.BankAccountResponse.ListBankAccountsResponse;
import com.eaglebank.web.dto.CreateBankAccountRequest;
import com.eaglebank.web.exception.DomainExceptions.ResourceNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Instant;
import java.util.List;

@Service
public class AccountService {

    private final BankAccountRepository accountRepository;
    private final UserRepository userRepository;
    private final Clock clock;

    public AccountService(
            BankAccountRepository accountRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.accountRepository = accountRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    @Transactional
    public BankAccountResponse createAccount(String authenticatedUserId, CreateBankAccountRequest request) {
        User user = findUser(authenticatedUserId);
        Instant now = Instant.now(clock);
        Money initialBalance = Money.zeroGbp();

        BankAccount account = new BankAccount(
                IdGenerator.nextAccountNumber(),
                BankAccount.DEFAULT_SORT_CODE,
                user,
                request.name(),
                request.accountType(),
                initialBalance.amount(),
                initialBalance.currency(),
                now,
                now
        );

        accountRepository.save(account);

        return BankAccountResponse.fromDomain(account);
    }

    @Transactional(readOnly = true)
    public ListBankAccountsResponse listAccounts(String authenticatedUserId) {
        List<BankAccountResponse> accounts = accountRepository
                .findAllByUserIdOrderByCreatedTimestampAsc(authenticatedUserId)
                .stream()
                .map(BankAccountResponse::fromDomain)
                .toList();

        return new ListBankAccountsResponse(accounts);
    }

    @Transactional(readOnly = true)
    public BankAccountResponse fetchAccountByAccountNumber(String accountNumber, String authenticatedUserId) {
        BankAccount account = findAccount(accountNumber);

        AccountSpecifications.assertOwnedBy(
                account,
                authenticatedUserId,
                "The user is not allowed to access the bank account details"
        );

        return BankAccountResponse.fromDomain(account);
    }

    private User findUser(String userId) {
        return userRepository
                .findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User was not found"));
    }

    private BankAccount findAccount(String accountNumber) {
        return accountRepository
                .findById(accountNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Bank account was not found"));
    }
}
