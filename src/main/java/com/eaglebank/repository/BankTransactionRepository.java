package com.eaglebank.repository;

import com.eaglebank.domain.BankTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BankTransactionRepository extends JpaRepository<BankTransaction, String> {

    List<BankTransaction> findAllByAccountNumberOrderByCreatedTimestampDesc(String accountNumber);

    Optional<BankTransaction> findByIdAndAccountNumber(String id, String accountNumber);
}
