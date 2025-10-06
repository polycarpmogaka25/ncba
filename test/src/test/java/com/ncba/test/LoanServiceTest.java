package com.ncba.test;

import com.ncba.test.entity.Account;
import com.ncba.test.model.LoanRequest;
import com.ncba.test.repo.AccountRepo;
import com.ncba.test.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class LoanServiceTest {
    @InjectMocks
    private LoanServiceImpl loanService;
    private AccountRepo accountRepository;

    @BeforeEach
    void setUp() {
        accountRepository = mock(AccountRepo.class);
        ReflectionTestUtils.setField(loanService, "minFunding", 500.0);
        ReflectionTestUtils.setField(loanService, "minLoanAmount", 1000.0);
        ReflectionTestUtils.setField(loanService, "maxLoanAmount", 10000.0);
    }

    @Test
    void applyForLoan_ShouldSucceedWithValidRequest() {

        var request = new LoanRequest();
        request.setAccountId(101L);
        request.setAmount(5000.0);
        request.setTenure(6);

        var account = new Account();
        account.setId(101L);
        account.setBalance(1000.0);

        when(accountRepository.findById(101L)).thenReturn(Optional.of(account));

        var response = loanService.applyForLoan(request).block();

        assertEquals(12, response.getSchedule().size());
        assertEquals(416.6666666666667, response.getSchedule().get(0).getAmount(), 0.01); // 5000/12
    }

    @Test
    void applyForLoan_ShouldThrowOnInsufficientFunding() {
        var request = new LoanRequest();
        request.setAccountId(1L);
        request.setAmount(5000.0);
        request.setTenure(12);
        Account account = new Account();
        account.setBalance(400.0);
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.of(account));

        assertThrows(RuntimeException.class, () -> loanService.applyForLoan(request));
    }

    @Test
    void applyForLoan_ShouldThrowOnInvalidAmount() {

        var request = new LoanRequest();
        request.setAccountId(1L);
        request.setAmount(500.0);
        request.setTenure(12);
        Account account = new Account();
        account.setBalance(1000.0);
        when(accountRepository.findById(1L)).thenReturn(java.util.Optional.of(account));


        assertThrows(RuntimeException.class, () -> loanService.applyForLoan(request));
    }
}
