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

import static org.hamcrest.Matchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class CustomerServiceTest {
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
    void register_ShouldCreatePendingCustomerAndSendEmail() {
        Register request = new Register();
        request.setName("John Doe");
        request.setEmail("john@example.com");
        request.setPassword("password123");

        Customer savedCustomer = new Customer();
        savedCustomer.setId(1L);
        savedCustomer.setStatus(CustomerStatus.PENDING_VERIFICATION);

        when(passwordEncoder.encode("password123")).thenReturn("hashedPass");
        when(utils.generateVerificationCode()).thenReturn("xrfctgyvhbjnk");
        when(customerRepository.save(any(Customer.class))).thenReturn(savedCustomer);

        // When & Then
        StepVerifier.create(customerService.register(request))
                .expectNextMatches(result ->
                        result.getId().equals(1L) &&
                                result.getName().equals("John Doe") &&
                                result.getEmail().equals("john@example.com") &&
                                result.getPassword().equals("hashedPass") &&
                                result.getVerificationCode().equals("xrfctgyvhbjnk") &&
                                CustomerStatus.PENDING_VERIFICATION.equals(result.getStatus())
                )
                .verifyComplete();

        verify(utils).sendVerificationEmailAsync(eq("john@example.com"), eq("xrfctgyvhbjnk"));
        verify(customerRepository).save(any(Customer.class));
    }

    @Test
    void verify_ShouldActivateCustomerAndCreateAccount() {
        var request = new VerifyRequest();
        request.setEmail("john@example.com");
        request.setVerificationCode("ABC123");
        Customer existing = new Customer();
        existing.setId(1L);
        existing.setEmail("john@example.com");
        existing.setVerificationCode("ABC123");
        existing.setStatus(CustomerStatus.PENDING_VERIFICATION);
        when(customerRepository.findByEmail("john@example.com")).thenReturn(java.util.Optional.of(existing));

        var result = customerService.verify(request);

        verify(accountService).createAccount(existing);
    }
}
