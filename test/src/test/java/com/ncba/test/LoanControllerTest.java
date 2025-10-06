package com.ncba.test;

import com.ncba.test.entity.Customer;
import com.ncba.test.model.LoanRequest;
import com.ncba.test.model.LoanResponse;
import com.ncba.test.model.Payment;
import com.ncba.test.service.LoanService;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class LoanControllerTest {
    @InjectMocks
    private LoanControllerTest controller;

    @Mock
    private LoanService loanService;

    private WebTestClient webTestClient;

    @AfterAll
    static void teardown() {
        Mockito.reset();
    }

    @BeforeEach
    void setup() {
        webTestClient = WebTestClient.bindToController(controller).build();
    }

    @Test
    void test_verify() {
        when(loanService.applyForLoan(
                any(LoanRequest.class)
        )).thenReturn(Mono.just(getResponse()));

        var baseUrl = "/api/v1/loans/apply";

        getWsResponseBodySpec(
                webTestClient.post().uri(baseUrl)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(getLoanRequest())
                        .exchange());

        verify(loanService).applyForLoan(
                any(LoanRequest.class));
    }

    private LoanResponse getResponse() {
        var payment = new Payment();
        payment.setAmount(500);
        payment.setMonth(9);
        return new LoanResponse(List.of(payment));
    }

    private LoanRequest getLoanRequest() {
        return LoanRequest.builder()
                .accountId(101L)
                .amount(5000.000)
                .tenure(12)
                .build();
    }

    private void getWsResponseBodySpec(WebTestClient.ResponseSpec webTestClient) {
        webTestClient
                .expectStatus().isOk();
    }

    private Customer getCustomer() {
        return Customer.builder()
                .name(UUID.randomUUID().toString())
                .email("m@g.com")
                .build();
    }
}
