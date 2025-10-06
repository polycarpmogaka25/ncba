package com.ncba.test.service.impl;

import com.ncba.test.entity.Customer;
import com.ncba.test.entity.CustomerStatus;
import com.ncba.test.model.Register;
import com.ncba.test.model.VerifyRequest;
import com.ncba.test.repo.CustomerRepo;
import com.ncba.test.service.CustomerService;
import com.ncba.test.utils.Util;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class CustomerServiceImpl implements CustomerService {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceImpl.class);

    private final CustomerRepo customerRepository;

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    private final Util utils;

    @Override
    public Mono<Customer> register(Register request) {
        log.info("Registering customer: {}", request.getEmail());
        return Mono.fromCallable(() -> {
                    var customer = new Customer();
                    customer.setName(request.getName());
                    customer.setEmail(request.getEmail());
                    customer.setPassword(passwordEncoder.encode(request.getPassword()));
                    customer.setVerificationCode(utils.generateVerificationCode());
                    return customer;
                })
                .map(customerRepository::save)
                .doOnSuccess(customer -> utils.sendVerificationEmailAsync(customer.getEmail(), customer.getVerificationCode()))
                .subscribeOn(Schedulers.boundedElastic());
    }

    @Override
    public Mono<Customer> verify(VerifyRequest request) {
        log.info("Verifying customer: {}", request.getEmail());
        return Mono.fromCallable(() -> customerRepository.findByEmail(request.getEmail())
                        .orElseThrow(() -> new RuntimeException("Customer not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(customer -> {
                    if (!customer.getVerificationCode().equals(request.getVerificationCode())) {
                        log.warn("Invalid verification code for {}", request.getEmail());
                        return Mono.error(new RuntimeException("Invalid verification code"));
                    }
                    customer.setStatus(CustomerStatus.ACTIVE);
                    customer.setVerificationCode(null);
                    return Mono.fromCallable(() -> customerRepository.save(customer))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnSuccess(accountService::createAccount)
                            .doOnSuccess(savedCustomer -> log.info("Customer verified and account created: ID {}", savedCustomer.getId()));
                });
    }
}
