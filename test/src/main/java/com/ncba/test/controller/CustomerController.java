package com.ncba.test.controller;

import com.ncba.test.entity.Customer;
import com.ncba.test.model.Register;
import com.ncba.test.model.VerifyRequest;
import com.ncba.test.service.CustomerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
public class CustomerController {

    private final CustomerService customerService;

    @PostMapping("/register")
    public Mono<ResponseEntity<Customer>> register(@Valid @RequestBody Register request) {
        return customerService.register(request)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }

    @PostMapping("/verify")
    public Mono<ResponseEntity<Customer>> verify(@Valid @RequestBody VerifyRequest request) {
        return customerService.verify(request)
                .map(ResponseEntity::ok)
                .onErrorReturn(ResponseEntity.badRequest().build());
    }
}
