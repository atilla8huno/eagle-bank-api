package com.eaglebank.repository;

import com.eaglebank.domain.BankAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface BankAccountRepository extends JpaRepository<BankAccount, String> {

    @Query("SELECT b FROM BankAccount b WHERE b.user.id = :userId ORDER BY b.createdTimestamp ASC")
    List<BankAccount> findAllByUserIdOrderByCreatedTimestampAsc(@Param("userId") String userId);
}
