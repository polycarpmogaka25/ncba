package com.ncba.test;

import com.ncba.test.entity.Customer;
import com.ncba.test.entity.CustomerStatus;
import com.ncba.test.model.Register;
import com.ncba.test.model.VerifyRequest;
import com.ncba.test.repo.CustomerRepo;
import com.ncba.test.service.impl.AccountService;
import com.ncba.test.service.impl.CustomerServiceImpl;
import com.ncba.test.utils.Util;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import reactor.test.StepVerifier;

import java.util.Optional;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerServiceTest {
    @Mock
    private CustomerRepo customerRepository;

    @Mock
    private AccountService accountService;

    @Mock
    private Util utils;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private CustomerServiceImpl customerService;


    @Test
    void register_ShouldSucceedAndSendEmail() {
        var request = new Register("Jane Doe", "jane@test.com", "securepwd");
        var encodedPassword = "ENCODED_HASH";
        var verificationCode = "123456";

        when(passwordEncoder.encode(request.getPassword())).thenReturn(encodedPassword);
        when(utils.generateVerificationCode()).thenReturn(verificationCode);

        when(customerRepository.save(any())).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setId(10L);
            return c;
        });

        StepVerifier.create(customerService.register(request))
                .expectNextMatches(cust ->
                        cust.getEmail().equals(request.getEmail()) &&
                                cust.getPassword().equals(encodedPassword) &&
                                cust.getVerificationCode().equals(verificationCode)
                )
                .verifyComplete();

        verify(utils, times(1)).sendVerificationEmailAsync(eq(request.getEmail()), eq(verificationCode));
    }

    @Test
    void verify_ShouldSucceedAndCreateAccount() {
        var email = "verified@test.com";
        var correctCode = "654321";
        var verifyRequest = new VerifyRequest(email, correctCode);

        var existingCustomer = new Customer(102L, "name", email, "password", correctCode, CustomerStatus.PENDING_VERIFICATION);

        when(customerRepository.findByEmail(email)).thenReturn(Optional.of(existingCustomer));

        when(customerRepository.save(existingCustomer)).thenAnswer(invocation -> {
            Customer c = invocation.getArgument(0);
            c.setStatus(CustomerStatus.ACTIVE);
            c.setVerificationCode(null);
            return c;
        });

        StepVerifier.create(customerService.verify(verifyRequest))
                .expectNextMatches(customer ->
                        customer.getStatus() == CustomerStatus.ACTIVE &&
                                customer.getVerificationCode() == null
                )
                .verifyComplete();

        verify(accountService, times(1)).createAccount(existingCustomer);
    }
}
