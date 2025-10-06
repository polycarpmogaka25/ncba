package com.ncba.test.service;

import com.ncba.test.model.LoanRequest;
import com.ncba.test.model.LoanResponse;
import jakarta.validation.Valid;
import reactor.core.publisher.Mono;

public interface LoanService {
    Mono<LoanResponse> applyForLoan(@Valid LoanRequest request);
}
