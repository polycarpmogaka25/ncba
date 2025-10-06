package com.ncba.test;

import com.ncba.test.controller.CustomerController;
import com.ncba.test.entity.Customer;
import com.ncba.test.model.Register;
import com.ncba.test.service.CustomerService;
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

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class ControllerTest {

    @InjectMocks
    private CustomerController controller;

    @Mock
    private CustomerService customerService;

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
        when(customerService.register(
                any(Register.class)
        )).thenReturn(Mono.just(getCustomer()));

        var baseUrl = "/api/v1/customers/register";

        getWsResponseBodySpec(
                webTestClient.post().uri(baseUrl)
                        .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                        .accept(MediaType.APPLICATION_JSON)
                        .bodyValue(getRegister())
                        .exchange());

        verify(customerService).register(
                any(Register.class));
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

    private Register getRegister() {
        Register register = new Register();
        register.setEmail("m@g.com");
        register.setName("Test");
        register.setPassword("12hjhjsjifui");
        return register;
    }
}
