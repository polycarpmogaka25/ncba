package com.ncba.test.service.impl;

import com.ncba.test.model.LoanRequest;
import com.ncba.test.model.LoanResponse;
import com.ncba.test.model.Payment;
import com.ncba.test.repo.AccountRepo;
import com.ncba.test.service.LoanService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class LoanServiceImpl implements LoanService {
    private static final Logger log = LoggerFactory.getLogger(LoanServiceImpl.class);

    private final AccountRepo accountRepository;

    @Value("${app.min-funding:500}")
    private double minFunding;

    @Value("${app.min-loan-amount:1000}")
    private double minLoanAmount;

    @Value("${app.max-loan-amount:10000}")
    private double maxLoanAmount;

    @Override
    public Mono<LoanResponse> applyForLoan(LoanRequest request) {
        log.info("Processing loan application for account: {} amount: {}", request.getAccountId(), request.getAmount());
        return Mono.fromCallable(() -> accountRepository.findById(request.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Account not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(account -> {
                    if (account.getBalance() < minFunding) {
                        log.warn("Insufficient funding for loan: balance {} < {}", account.getBalance(), minFunding);
                        return Mono.error(new RuntimeException("Account must be funded with at least KES " + minFunding));
                    }
                    if (request.getAmount() < minLoanAmount || request.getAmount() > maxLoanAmount) {
                        log.warn("Loan amount out of range: {} (min: {}, max: {})", request.getAmount(), minLoanAmount, maxLoanAmount);
                        return Mono.error(new RuntimeException("Loan amount must be between KES " + minLoanAmount + " and KES " + maxLoanAmount));
                    }
                    // Mock call to Core Banking Service
                    List<Payment> schedule = generateLoanSchedule(request.getAmount(), request.getTenure());
                    log.info("Loan approved. Schedule generated with {} installments", schedule.size());
                    return Mono.just(new LoanResponse(schedule));
                });
    }

    private List<Payment> generateLoanSchedule(double amount, int tenure) {
        var monthlyPayment = amount / tenure;
        var schedule = new ArrayList<Payment>();
        for (int i = 1; i <= tenure; i++) {
            var payment = new Payment(i, monthlyPayment);
            schedule.add(payment);
        }
        return schedule;
    }
}
