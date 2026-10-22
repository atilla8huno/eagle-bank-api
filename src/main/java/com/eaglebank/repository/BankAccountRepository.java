package com.eaglebank.repository;

import com.eaglebank.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {
    List<BankAccount> findAllByUserIdOrderByCreatedTimestampAsc(String userId);
}
