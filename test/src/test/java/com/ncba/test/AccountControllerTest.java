package com.ncba.test;

import com.ncba.test.controller.AccountController;
import com.ncba.test.entity.Account;
import com.ncba.test.model.FundRequest;
import com.ncba.test.service.impl.AccountService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class AccountControllerTest {

    @InjectMocks
    private AccountController controller;

    @Mock
    private AccountService accountService;

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
    void test_register() {
        when(accountService.fundAccount(
                any(FundRequest.class)
        )).thenReturn(Mono.just(getAccount()));

        var baseUrl = "/api/v1/accounts/accounts";

        getWsResponseBodySpec(
                webTestClient.post().uri(baseUrl)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(getRequest())
                        .exchange());

        verify(accountService).fundAccount(any(FundRequest.class));
    }

    private FundRequest getRequest() {
        FundRequest fundRequest = new FundRequest();
        fundRequest.setAccountId(101L);
        fundRequest.setAmount(5000.000);
        return fundRequest;
    }


    private void getWsResponseBodySpec(WebTestClient.ResponseSpec webTestClient) {
        webTestClient
                .expectStatus().isOk();
    }

    private Account getAccount() {
        return Account.builder()
                .balance(450.00)
                .id(101L)
                .build();
    }


}
