package com.ncba.test;

import com.ncba.test.entity.Account;
import com.ncba.test.model.FundRequest;
import com.ncba.test.repo.AccountRepo;
import com.ncba.test.service.impl.AccountService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {
    @Mock
    private AccountRepo accountRepository;

    @InjectMocks
    private AccountService accountService;

    @Test
    void fundAccount_ShouldSucceedAndIncreaseBalance() {
        var accountId = 1L;
        var initialBalance = 100.00;
        var amountToFund = 50.00;
        var expectedBalance = initialBalance + amountToFund;

        var existingAccount = new Account();
        existingAccount.setId(accountId);
        existingAccount.setBalance(initialBalance);
        var fundRequest = new FundRequest();
        fundRequest.setAmount(amountToFund);
        fundRequest.setAccountId(accountId);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        StepVerifier.create(accountService.fundAccount(fundRequest))
                .expectNextMatches(account -> {
                    assertNotNull(account.getId());
                    assertEquals(expectedBalance, account.getBalance());
                    return true;
                })
                .verifyComplete();

        verify(accountRepository, times(1)).save(any(Account.class));
    }

    @Test
    void fundAccount_ShouldThrowIfAccountNotFound() {
        var nonExistentId = 999L;
        var fundRequest = new FundRequest();
        fundRequest.setAccountId(nonExistentId);
        fundRequest.setAmount(500.00);

        when(accountRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        StepVerifier.create(accountService.fundAccount(fundRequest))
                .verifyErrorMessage("Account not found");

        verify(accountRepository, times(0)).save(any(Account.class));
    }

    @Test
    void fundAccount_ShouldThrowIfAmountIsNotPositive() {
        var accountId = 1L;
        var existingAccount = new Account();
        existingAccount.setId(accountId);
        existingAccount.setBalance(100.00);
        var fundRequest = new FundRequest();
        fundRequest.setAccountId(accountId);
        fundRequest.setAmount(-10.00);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(existingAccount));

        StepVerifier.create(accountService.fundAccount(fundRequest))
                .verifyErrorMessage("Amount must be positive");

        verify(accountRepository, times(0)).save(any(Account.class));
    }
}
