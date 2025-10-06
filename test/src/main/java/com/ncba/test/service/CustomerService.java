package com.ncba.test.service;

import com.ncba.test.entity.Customer;
import com.ncba.test.model.Register;
import com.ncba.test.model.VerifyRequest;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

public interface CustomerService {
    Mono<Customer> register(@Valid Register request);

    Mono<Customer> verify(@Valid VerifyRequest request);
}
