package com.ncba.test.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class Util {
    private static final Logger log = LoggerFactory.getLogger(Util.class);

    @Value("${app.verification-code-length:6}")
    private int codeLength;

    public String generateVerificationCode() {
        return UUID.randomUUID().toString().replaceAll("-", "").substring(0, codeLength).toUpperCase();
    }

    @Async
    public void sendVerificationEmailAsync(String email, String code) {

        log.info("Mock email sent to {} with verification code: {}", email, code);
        // Simulate delay
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
