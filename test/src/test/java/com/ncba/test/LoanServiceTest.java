package com.ncba.test;

import com.ncba.test.entity.Account;
import com.ncba.test.entity.Customer;
import com.ncba.test.model.LoanRequest;
import com.ncba.test.repo.AccountRepo;
import com.ncba.test.service.impl.LoanServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;
import reactor.test.StepVerifier;

import java.util.Optional;
import java.util.UUID;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanServiceTest {

    private final double minFunding = 1000.00;
    private final double minLoanAmount = 5000.00;
    private final double maxLoanAmount = 100000.00;
    @InjectMocks
    private LoanServiceImpl loanService;
    @Mock
    private AccountRepo accountRepository;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(loanService, "minFunding", minFunding);
        ReflectionTestUtils.setField(loanService, "minLoanAmount", minLoanAmount);
        ReflectionTestUtils.setField(loanService, "maxLoanAmount", maxLoanAmount);
    }

    @Test
    void applyForLoan_ShouldSucceedWithValidRequest() {
        var accountId = 123L;
        var validAmount = 10000.00;

        var mockAccount = new Account();
        mockAccount.setId(accountId);
        mockAccount.setCustomer(getCustomer());
        mockAccount.setBalance(5500.00);

        var validRequest = new LoanRequest(accountId, validAmount, 12);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(mockAccount));

        StepVerifier.create(loanService.applyForLoan(validRequest))
                .expectNextMatches(response ->
                        response.getSchedule() != null && !response.getSchedule().isEmpty())
                .verifyComplete();
    }

    @Test
    void applyForLoan_ShouldThrowOnInsufficientFunding() {
        var accountId = 1L;
        var validAmount = 5000.0;

        var request = new LoanRequest(accountId, validAmount, 12);

        var account = new Account();
        account.setId(accountId);
        account.setBalance(400.0);

        when(accountRepository.findById(accountId)).thenReturn(Optional.of(account));

        StepVerifier.create(loanService.applyForLoan(request))
                .verifyErrorMessage("Account must be funded with at least KES " + Double.toString(minFunding));
    }

    @Test
    void testAccountNotFound() {
        var accountId = 999L;
        var requestAmount = 50000.00;

        var request = new LoanRequest(accountId, requestAmount, 12);

        when(accountRepository.findById(accountId)).thenReturn(Optional.empty());

        StepVerifier.create(loanService.applyForLoan(request))
                .verifyErrorMessage("Account not found");
    }

    private Customer getCustomer() {
        return Customer.builder()
                .name(UUID.randomUUID().toString())
                .email("m@g.com")
                .build();
    }
}
