package com.ncba.test.service.impl;

import com.ncba.test.entity.Account;
import com.ncba.test.entity.Customer;
import com.ncba.test.model.FundRequest;
import com.ncba.test.repo.AccountRepo;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Service
@RequiredArgsConstructor
public class AccountService {
    private static final Logger log = LoggerFactory.getLogger(AccountService.class);

    private final AccountRepo accountRepository;

    public void createAccount(Customer customer) {
        log.info("Creating account for customer: ID {}", customer.getId());
        Account account = new Account();
        account.setCustomer(customer);
        account = accountRepository.save(account);
        log.info("Account created: ID {}", account.getId());
    }

    public Mono<Account> fundAccount(FundRequest request) {
        log.info("Funding account: ID {} with amount {}", request.getAccountId(), request.getAmount());
        return Mono.fromCallable(() -> accountRepository.findById(request.getAccountId())
                        .orElseThrow(() -> new RuntimeException("Account not found")))
                .subscribeOn(Schedulers.boundedElastic())
                .flatMap(account -> {
                    if (request.getAmount() <= 0) {
                        return Mono.error(new RuntimeException("Amount must be positive"));
                    }
                    account.setBalance(account.getBalance() + request.getAmount());
                    return Mono.fromCallable(() -> accountRepository.save(account))
                            .subscribeOn(Schedulers.boundedElastic())
                            .doOnSuccess(savedAccount -> log.info("Account funded. New balance: {}", savedAccount.getBalance()));
                });
    }
}
