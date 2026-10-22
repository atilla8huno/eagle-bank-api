package com.eaglebank.service.strategy;

import com.eaglebank.domain.Money;
import com.eaglebank.domain.TransactionType;
import com.eaglebank.web.exception.DomainExceptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

@DisplayName("Test suite of the class: TransactionOperation")
class TransactionOperationTest {

    @Test
    @DisplayName("Should apply withdrawal operation and enforce balance limits")
    void givenBalanceAndAmount_whenApplyingWithdrawalOperation_thenShouldUpdateOrThrow() {
        //given
        TransactionOperation withdrawalOp = TransactionOperation.forType(TransactionType.WITHDRAWAL);
        Money start = Money.gbp(new BigDecimal("500.00"));

        //when
        Money afterWithdrawal = withdrawalOp.apply(start, Money.gbp(new BigDecimal("150.25")));

        Executable withdrawalOverBalance =
                () -> withdrawalOp.apply(Money.gbp(new BigDecimal("50.00")), Money.gbp(new BigDecimal("50.01")));

        //then
        assertInstanceOf(TransactionOperation.Withdrawal.class, withdrawalOp);
        assertEquals(new BigDecimal("349.75"), afterWithdrawal.amount());
        assertThrows(DomainExceptions.InsufficientFundsException.class, withdrawalOverBalance);
    }

    @Test
    @DisplayName("Should apply deposit operation and enforce balance limits")
    void givenBalanceAndAmount_whenApplyingDepositOperation_thenShouldUpdateOrThrow() {
        //given
        TransactionOperation depositOp = TransactionOperation.forType(TransactionType.DEPOSIT);
        Money start = Money.gbp(new BigDecimal("500.00"));

        //when
        Money afterDeposit = depositOp.apply(start, Money.gbp(new BigDecimal("250.25")));

        Executable depositOverMax =
                () -> depositOp.apply(Money.gbp(new BigDecimal("9900.00")), Money.gbp(new BigDecimal("200.00")));

        //then
        assertInstanceOf(TransactionOperation.Deposit.class, depositOp);
        assertEquals(new BigDecimal("750.25"), afterDeposit.amount());
        assertThrows(DomainExceptions.InvalidTransactionException.class, depositOverMax);
    }
}
