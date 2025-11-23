package com.auth;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for EmailValidator performance.
 * Tests email validation throughput for username field.
 * Performance target: Email validation should not be a bottleneck in login flow.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class EmailValidatorBenchmark {

    private String validEmail;
    private String invalidEmailNoAt;
    private String invalidEmailNoDot;
    private String nullEmail;
    private String longEmail;
    private String[] bulkEmails;

    @Setup
    public void setup() {
        validEmail = "usuario@ejemplo.com";
        invalidEmailNoAt = "usuarioejemplo.com";
        invalidEmailNoDot = "usuario@ejemplocom";
        nullEmail = null;
        longEmail = "very.long.email.address.with.multiple.dots@subdomain.example.com";
        
        // Bulk emails for load testing (simulating multiple concurrent login attempts)
        bulkEmails = new String[1000];
        for (int i = 0; i < 1000; i++) {
            if (i % 4 == 0) {
                bulkEmails[i] = "user" + i + "@domain.com"; // valid
            } else if (i % 4 == 1) {
                bulkEmails[i] = "invalidemail" + i; // no @ or .
            } else if (i % 4 == 2) {
                bulkEmails[i] = "user" + i + "@nodot"; // no .
            } else {
                bulkEmails[i] = null; // edge case
            }
        }
    }

    @Benchmark
    public boolean testValidEmail() {
        return EmailValidator.isValid(validEmail);
    }

    @Benchmark
    public boolean testInvalidEmailNoAt() {
        return EmailValidator.isValid(invalidEmailNoAt);
    }

    @Benchmark
    public boolean testInvalidEmailNoDot() {
        return EmailValidator.isValid(invalidEmailNoDot);
    }

    @Benchmark
    public boolean testNullEmail() {
        return EmailValidator.isValid(nullEmail);
    }

    @Benchmark
    public boolean testLongEmail() {
        return EmailValidator.isValid(longEmail);
    }

    /**
     * Simulate validation of a batch of mixed emails.
     * Represents real-world usage where valid/invalid emails are intermixed.
     */
    @Benchmark
    public int testMixedEmails() {
        int validCount = 0;
        if (EmailValidator.isValid(validEmail)) validCount++;
        if (EmailValidator.isValid(invalidEmailNoAt)) validCount++;
        if (EmailValidator.isValid(invalidEmailNoDot)) validCount++;
        if (EmailValidator.isValid(nullEmail)) validCount++;
        if (EmailValidator.isValid(longEmail)) validCount++;
        return validCount;
    }

    /**
     * High-load scenario: Validate 1000 emails.
     * Simulates concurrent login attempts during peak load.
     * Performance target: Should handle >10K emails/second.
     */
    @Benchmark
    @OperationsPerInvocation(1000)
    public int testHighLoadEmailValidation() {
        int validCount = 0;
        for (String email : bulkEmails) {
            if (EmailValidator.isValid(email)) {
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * Worst-case scenario: Repeated validation of same email.
     * Tests caching potential and validation consistency.
     */
    @Benchmark
    @OperationsPerInvocation(10000)
    public int testRepeatedSameEmail() {
        int validCount = 0;
        for (int i = 0; i < 10000; i++) {
            if (EmailValidator.isValid(validEmail)) {
                validCount++;
            }
        }
        return validCount;
    }
}
