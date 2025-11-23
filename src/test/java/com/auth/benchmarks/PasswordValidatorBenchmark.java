package com.auth;

import org.openjdk.jmh.annotations.*;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for PasswordValidator performance (Requirement 4).
 * Tests password validation against specific rules:
 * - Length: 5-10 characters (4.1, 4.2)
 * - At least one uppercase letter (4.3)
 * - At least one special character (4.4)
 * Performance target: Validation should complete in microseconds to not impact UX.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 3, time = 1)
@Measurement(iterations = 5, time = 1)
@Fork(1)
public class PasswordValidatorBenchmark {

    private String validPassword;
    private String invalidTooShort;
    private String invalidTooLong;
    private String invalidNoUppercase;
    private String invalidNoSpecial;
    private String nullPassword;
    private String complexPassword;
    private String[] commonPasswords;
    private String[] attackPasswords;

    @Setup
    public void setup() {
        // Requirement 4: 5-10 chars, 1 uppercase, 1 special
        validPassword = "Abc!1"; // 5 chars, min length
        invalidTooShort = "Ab!1"; // 4 chars, below min (4.1)
        invalidTooLong = "Abc!123456789"; // 13 chars, above max (4.2)
        invalidNoUppercase = "abc!12345"; // Missing uppercase (4.3)
        invalidNoSpecial = "Abc123456"; // Missing special char (4.4)
        nullPassword = null;
        complexPassword = "P@ssW0rd!#"; // 10 chars, max length
        
        // Common passwords users might try (all should fail validation)
        commonPasswords = new String[] {
            "password", "12345", "admin", "user", "test",
            "Password", "Pass123", "Test1234", "User123", "Admin1"
        };
        
        // Simulated brute force attack passwords
        attackPasswords = new String[5000];
        for (int i = 0; i < 5000; i++) {
            // Generate various invalid passwords for attack simulation
            attackPasswords[i] = "Pass" + i + "!";
        }
    }

    @Benchmark
    public boolean testValidPassword() {
        return PasswordValidator.isValid(validPassword);
    }

    @Benchmark
    public boolean testInvalidTooShort() {
        return PasswordValidator.isValid(invalidTooShort);
    }

    @Benchmark
    public boolean testInvalidTooLong() {
        return PasswordValidator.isValid(invalidTooLong);
    }

    @Benchmark
    public boolean testInvalidNoUppercase() {
        return PasswordValidator.isValid(invalidNoUppercase);
    }

    @Benchmark
    public boolean testInvalidNoSpecial() {
        return PasswordValidator.isValid(invalidNoSpecial);
    }

    @Benchmark
    public boolean testNullPassword() {
        return PasswordValidator.isValid(nullPassword);
    }

    @Benchmark
    public boolean testComplexPassword() {
        return PasswordValidator.isValid(complexPassword);
    }

    /**
     * Simulate validation of multiple passwords in a batch.
     * Represents login attempt validation scenarios.
     */
    @Benchmark
    public int testBatchValidation() {
        int validCount = 0;
        if (PasswordValidator.isValid(validPassword)) validCount++;
        if (PasswordValidator.isValid(invalidTooShort)) validCount++;
        if (PasswordValidator.isValid(invalidNoUppercase)) validCount++;
        if (PasswordValidator.isValid(invalidNoSpecial)) validCount++;
        if (PasswordValidator.isValid(complexPassword)) validCount++;
        return validCount;
    }

    /**
     * Stress test with repeated validation calls.
     * Simulates high-frequency password checking.
     */
    @Benchmark
    @OperationsPerInvocation(100)
    public int testRepeatedValidation() {
        int validCount = 0;
        for (int i = 0; i < 100; i++) {
            if (PasswordValidator.isValid(validPassword)) {
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * Validate common weak passwords.
     * Tests performance when rejecting frequently used invalid passwords.
     */
    @Benchmark
    public int testCommonWeakPasswords() {
        int validCount = 0;
        for (String pwd : commonPasswords) {
            if (PasswordValidator.isValid(pwd)) {
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * Brute force attack simulation (Requirement 6 related).
     * Tests validator performance under attack scenario with 5000 attempts.
     * Performance target: Must maintain throughput even under attack load.
     */
    @Benchmark
    @OperationsPerInvocation(5000)
    public int testBruteForceAttackValidation() {
        int validCount = 0;
        for (String pwd : attackPasswords) {
            if (PasswordValidator.isValid(pwd)) {
                validCount++;
            }
        }
        return validCount;
    }

    /**
     * Edge case: Minimum and maximum length boundaries.
     * Tests performance at validation rule boundaries (4.1, 4.2).
     */
    @Benchmark
    public boolean testLengthBoundaries() {
        // Test exactly 5 chars (minimum)
        boolean min = PasswordValidator.isValid("Abc!1");
        // Test exactly 10 chars (maximum)
        boolean max = PasswordValidator.isValid("Abcd!12345");
        // Test 4 chars (below minimum)
        boolean below = PasswordValidator.isValid("Ab!1");
        // Test 11 chars (above maximum)
        boolean above = PasswordValidator.isValid("Abcd!123456");
        return min && max && !below && !above;
    }
}
