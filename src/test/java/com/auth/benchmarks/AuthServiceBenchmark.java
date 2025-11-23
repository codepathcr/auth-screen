package com.auth;

import org.openjdk.jmh.annotations.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for AuthService performance using in-memory H2 database.
 * - Email-based login
 * - Password validation
 * - Password recovery
 * - 5 failed attempts blocking
 * - Database operations
 * Performance targets: 
 * - Single login: <50ms
 * - Concurrent logins: >100 ops/second
 * - Failed attempt handling: No degradation up to 5 attempts
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
@Fork(1)
public class AuthServiceBenchmark {

    private AuthService authService;
    private Connection connection;
    private String validEmail;
    private String validPassword;
    private String invalidEmail;
    private String invalidPassword;

    @Setup(Level.Trial)
    public void setupDatabase() throws Exception {
        // Use H2 in-memory database for isolated performance testing
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1", "sa", "");
        
        // Create table structure
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(
                "CREATE TABLE usuarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "email VARCHAR(255) UNIQUE NOT NULL, " +
                "clave_hash VARCHAR(255) NOT NULL, " +
                "intentos_fallidos INT NOT NULL DEFAULT 0, " +
                "bloqueado BOOLEAN NOT NULL DEFAULT FALSE, " +
                "fecha_creacion TIMESTAMP DEFAULT NOW())"
            );
            
            // Insert test user
            stmt.execute(
                "INSERT INTO usuarios (email, clave_hash) " +
                "VALUES ('usuario@ejemplo.com', 'Abc!1')"
            );
        }

        authService = new AuthService();
        validEmail = "usuario@ejemplo.com";
        validPassword = "Abc!1";
        invalidEmail = "noexiste@ejemplo.com";
        invalidPassword = "WrongPass1!";
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    /**
     * Reset user state between iterations to ensure consistent results.
     */
    @Setup(Level.Iteration)
    public void resetUserState() throws Exception {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE");
        }
    }

    @Benchmark
    public String testSuccessfulLogin() {
        return authService.loginWithConnection(connection, validEmail, validPassword);
    }

    @Benchmark
    public String testInvalidEmail() {
        return authService.loginWithConnection(connection, "invalid-email", validPassword);
    }

    @Benchmark
    public String testInvalidPassword() {
        return authService.loginWithConnection(connection, validEmail, "short");
    }

    @Benchmark
    public String testUserNotFound() {
        return authService.loginWithConnection(connection, invalidEmail, validPassword);
    }

    @Benchmark
    public String testWrongPassword() {
        return authService.loginWithConnection(connection, validEmail, invalidPassword);
    }

    /**
     * Benchmark password recovery operation.
     */
    @Benchmark
    public String testPasswordRecovery() {
        return authService.recoverPasswordWithConnection(connection, validEmail);
    }

    /**
     * Mixed scenario representing typical usage patterns.
     * 60% successful logins, 30% wrong password, 10% invalid input.
     */
    @Benchmark
    @OperationsPerInvocation(10)
    public int testMixedScenario() {
        int operations = 0;
        
        // 6 successful logins
        for (int i = 0; i < 6; i++) {
            authService.loginWithConnection(connection, validEmail, validPassword);
            operations++;
        }
        
        // 3 wrong passwords
        for (int i = 0; i < 3; i++) {
            authService.loginWithConnection(connection, validEmail, invalidPassword);
            operations++;
        }
        
        // 1 invalid email
        authService.loginWithConnection(connection, "invalid", validPassword);
        operations++;
        
        return operations;
    }

    /**
     * Test progressive failed login attempts (1-5 attempts).
     * Measures performance degradation as failed attempts increase.
     * Critical: Performance should remain stable through all 5 attempts.
     */
    @Benchmark
    public String testFailedLoginAttempts_5Times() throws Exception {
        String result = null;
        // Attempt 1
        result = authService.loginWithConnection(connection, validEmail, invalidPassword);
        // Attempt 2
        result = authService.loginWithConnection(connection, validEmail, invalidPassword);
        // Attempt 3
        result = authService.loginWithConnection(connection, validEmail, invalidPassword);
        // Attempt 4
        result = authService.loginWithConnection(connection, validEmail, invalidPassword);
        // Attempt 5 - should block
        result = authService.loginWithConnection(connection, validEmail, invalidPassword);
        
        // Reset for next iteration
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE");
        }
        return result;
    }

    /**
     * Requirement 6: Test account blocking after 5 failed attempts.
     * Verifies blocking mechanism doesn't cause performance issues.
     */
    @Benchmark
    public boolean testAccountBlockingPerformance() throws Exception {
        // Make 5 failed attempts to trigger blocking
        for (int i = 0; i < 5; i++) {
            authService.loginWithConnection(connection, validEmail, invalidPassword);
        }
        
        // Verify subsequent login attempts are rejected quickly
        String result = authService.loginWithConnection(connection, validEmail, validPassword);
        boolean isBlocked = result.contains("bloqueada");
        
        // Reset for next iteration
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE");
        }
        
        return isBlocked;
    }

    /**
     * Requirement 5: Password recovery performance test.
     * Measures email validation + database lookup throughput.
     */
    @Benchmark
    public String testPasswordRecoveryThroughput() {
        return authService.recoverPasswordWithConnection(connection, validEmail);
    }

    /**
     * Requirement 7: Database query performance.
     * Tests the usuarios table query performance.
     */
    @Benchmark
    public int testDatabaseQueryPerformance() throws Exception {
        int count = 0;
        try (Statement stmt = connection.createStatement();
             var rs = stmt.executeQuery("SELECT id, email, clave_hash, intentos_fallidos, bloqueado FROM usuarios")) {
            while (rs.next()) {
                count++;
            }
        }
        return count;
    }

    /**
     * High concurrency simulation: Multiple users logging in simultaneously.
     * Tests system behavior under peak load (50 operations).
     */
    @Benchmark
    @OperationsPerInvocation(50)
    public int testHighConcurrencyLogin() {
        int successCount = 0;
        for (int i = 0; i < 50; i++) {
            String result = authService.loginWithConnection(connection, validEmail, validPassword);
            if (result.contains("exitoso")) {
                successCount++;
            }
        }
        return successCount;
    }

    /**
     * Attack simulation: Rapid failed login attempts.
     * Simulates brute force attack to test system resilience.
     */
    @Benchmark
    @OperationsPerInvocation(20)
    public int testBruteForceAttackSimulation() throws Exception {
        int blockedCount = 0;
        
        // Simulate attacker trying 20 wrong passwords
        for (int i = 0; i < 20; i++) {
            String result = authService.loginWithConnection(connection, validEmail, "WrongPass" + i + "!");
            if (result.contains("bloqueada")) {
                blockedCount++;
            }
        }
        
        // Reset for next iteration
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("UPDATE usuarios SET intentos_fallidos = 0, bloqueado = FALSE");
        }
        
        return blockedCount;
    }
}
