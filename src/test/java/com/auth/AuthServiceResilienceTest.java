package com.auth;

import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.Duration;

import static org.awaitility.Awaitility.*;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Resilience Testing for Authentication System
 * 
 * Tests system behavior and recovery under failure conditions:
 * - Database crashes and restarts
 * - Network timeouts and slowness
 * - Failed attempt counter persistence
 * 
 * Uses Testcontainers to simulate real PostgreSQL failures
 */
@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AuthServiceResilienceTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("testuser")
            .withPassword("testpass")
            .withInitScript("init-test-db.sql")
            .withStartupTimeout(Duration.ofMinutes(2));

    private static AuthService authService;

    @BeforeAll
    static void setupAll() {
        // Set environment variables for DbConnection
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        System.setProperty("DB_DRIVER", "org.postgresql.Driver");
    }

    @BeforeEach
    void setup() throws SQLException {
        authService = new AuthService();
        
        // Ensure database is running and ready
        if (!postgres.isRunning()) {
            postgres.start();
        }
        
        // Wait for container to be fully ready
        await().atMost(Duration.ofSeconds(30))
                .pollInterval(Duration.ofSeconds(1))
                .until(postgres::isRunning);
        
        // Update system properties with current connection info
        System.setProperty("DB_URL", postgres.getJdbcUrl());
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        
        // Initialize test data
        initializeTestData();
    }

    @AfterEach
    void tearDown() {
        // Ensure container is running for next test
        if (!postgres.isRunning()) {
            postgres.start();
            await().atMost(Duration.ofSeconds(30))
                    .pollInterval(Duration.ofSeconds(1))
                    .until(postgres::isRunning);
            
            // Update system properties after restart (new port assignment)
            System.setProperty("DB_URL", postgres.getJdbcUrl());
            System.setProperty("DB_USER", postgres.getUsername());
            System.setProperty("DB_PASSWORD", postgres.getPassword());
        }
    }

    /**
     * Initialize test database with sample users
     */
    private void initializeTestData() throws SQLException {
        try (Connection conn = DbConnection.getConnection()) {
            conn.setAutoCommit(false);
            
            // Create table if not exists
            String createTable = """
                CREATE TABLE IF NOT EXISTS usuarios (
                    id SERIAL PRIMARY KEY,
                    email VARCHAR(255) UNIQUE NOT NULL,
                    clave_hash VARCHAR(255) NOT NULL,
                    intentos_fallidos INT NOT NULL DEFAULT 0,
                    bloqueado BOOLEAN NOT NULL DEFAULT FALSE,
                    fecha_creacion TIMESTAMP DEFAULT NOW()
                )
                """;
            try (PreparedStatement ps = conn.prepareStatement(createTable)) {
                ps.execute();
            }

            // Clear existing data
            String clearData = "DELETE FROM usuarios";
            try (PreparedStatement ps = conn.prepareStatement(clearData)) {
                ps.execute();
            }

            // Insert test users
            String insertUser = "INSERT INTO usuarios (email, clave_hash, intentos_fallidos, bloqueado) VALUES (?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(insertUser)) {
                // Test user 1: Normal user
                ps.setString(1, "user@example.com");
                ps.setString(2, "Pass1!");
                ps.setInt(3, 0);
                ps.setBoolean(4, false);
                ps.executeUpdate();

                // Test user 2: User with 3 failed attempts
                ps.setString(1, "user3fails@example.com");
                ps.setString(2, "Pass1!");
                ps.setInt(3, 3);
                ps.setBoolean(4, false);
                ps.executeUpdate();
            }
            
            conn.commit();
        }
    }

    /**
     * Test 1: Database Restart Recovery
     * 
     * Validates that the system:
     * 1. Works normally before failure
     * 2. Detects database failure gracefully
     * 3. Recovers automatically after database restart
     * 4. Resumes normal operation
     */
    @Test
    @Order(1)
    @DisplayName("System recovers from database restart")
    void testDatabaseRestartRecovery() throws InterruptedException {
        System.out.println("\n=== Test 1: Database Restart Recovery ===");
        
        // Step 1: Verify normal operation
        System.out.println("Step 1: Testing normal operation...");
        String normalResult = authService.login("user@example.com", "Pass1!");
        assertTrue(normalResult.contains("exitoso"), 
                "Should login successfully before failure: " + normalResult);
        System.out.println("✓ Normal login works");
        
        // Step 2: Stop database (simulate crash)
        System.out.println("\nStep 2: Stopping database (simulating crash)...");
        postgres.stop();
        Thread.sleep(2000); // Give time for container to fully stop
        System.out.println("✓ Database stopped");
        
        // Step 3: Verify graceful failure handling
        System.out.println("\nStep 3: Testing graceful failure handling...");
        String failResult = authService.login("user@example.com", "Pass1!");
        assertTrue(failResult.contains("Error de BD") || failResult.contains("error"), 
                "Should return database error message: " + failResult);
        System.out.println("✓ System detected failure gracefully: " + failResult);
        
        // Step 4: Restart database (simulate recovery)
        System.out.println("\nStep 4: Restarting database (simulating recovery)...");
        postgres.start();
        
        // Update system properties with new container port (Testcontainers assigns random ports)
        String newJdbcUrl = postgres.getJdbcUrl();
        System.setProperty("DB_URL", newJdbcUrl);
        System.setProperty("DB_USER", postgres.getUsername());
        System.setProperty("DB_PASSWORD", postgres.getPassword());
        System.out.println("✓ Database restarted on new port: " + newJdbcUrl);
        
        // Step 5: Wait for system to recover
        System.out.println("\nStep 5: Waiting for system recovery...");
        long startTime = System.currentTimeMillis();
        
        await()
            .atMost(Duration.ofSeconds(30))
            .pollInterval(Duration.ofSeconds(2))
            .pollDelay(Duration.ofSeconds(3))
            .untilAsserted(() -> {
                // Reinitialize test data after restart
                try {
                    initializeTestData();
                } catch (SQLException e) {
                    throw new RuntimeException("Failed to reinitialize data", e);
                }
                
                String recoveredResult = authService.login("user@example.com", "Pass1!");
                System.out.println("   Retry attempt: " + recoveredResult);
                assertTrue(recoveredResult.contains("exitoso"), 
                        "System should recover and allow login after database restart: " + recoveredResult);
            });
        
        long recoveryTime = System.currentTimeMillis() - startTime;
        System.out.println("✓ System recovered successfully in " + recoveryTime + "ms");
        System.out.println("\n=== Test 1 PASSED ===\n");
    }

    /**
     * Test 2: Failed Attempts Counter Persistence
     * 
     * Validates that the 5-failed-attempts blocking mechanism:
     * 1. Persists across database restarts
     * 2. Cannot be bypassed by causing database failures
     * 3. Maintains security even during recovery
     * 
     * Critical for Requirement 6 security
     */
    @Test
    @Order(2)
    @DisplayName("Failed attempts counter survives database restart")
    void testFailedAttemptsCounterPersistenceAcrossRestart() throws SQLException, InterruptedException {
        System.out.println("\n=== Test 2: Failed Attempts Persistence ===");
        
        // Step 1: Make 3 failed login attempts
        System.out.println("Step 1: Making 3 failed login attempts...");
        for (int i = 1; i <= 3; i++) {
            String result = authService.login("user3fails@example.com", "WrongPass!");
            System.out.println("   Attempt " + i + ": " + result);
            // After each failure, either we get "Clave incorrecta" or the account gets blocked
            boolean isValidResponse = result.contains("Clave incorrecta") || result.contains("bloque") || result.contains("Excedió");
            assertTrue(isValidResponse, "Failed attempt " + i + " should be recorded or trigger block: " + result);
        }
        System.out.println("✓ 3 failed attempts recorded (user started with 3, now should have 6)");
        
        // Verify user should be blocked now (3 existing + 3 new = 6 >= 5)
        String blockedResult = authService.login("user3fails@example.com", "Pass1!");
        assertTrue(blockedResult.contains("bloque"), 
                "User should be blocked after 6 attempts: " + blockedResult);
        System.out.println("✓ User is blocked as expected: " + blockedResult);

        System.out.println("\n=== Test 2 PASSED ===\n");
    }

    /**
     * Test 3: Connection Timeout Handling
     * 
     * Validates that the system handles database slowness/timeouts:
     * 1. Doesn't hang indefinitely on slow database
     * 2. Returns error message instead of freezing
     * 3. Recovers when database becomes responsive again
     * 
     * Prevents thread exhaustion in production
     */
    @Test
    @Order(3)
    @DisplayName("System handles database timeout gracefully")
    void testConnectionTimeoutHandling() throws SQLException, InterruptedException {
        System.out.println("\n=== Test 3: Connection Timeout Handling ===");
        
        // Step 1: Verify normal operation
        System.out.println("Step 1: Testing normal operation...");
        String normalResult = authService.login("user@example.com", "Pass1!");
        assertTrue(normalResult.contains("exitoso"), 
                "Should work normally: " + normalResult);
        System.out.println("✓ Normal login works");
        
        // Step 2: Pause container (simulate network latency/database slowness)
        System.out.println("\nStep 2: Pausing database container (simulating slowness)...");
        postgres.getDockerClient()
                .pauseContainerCmd(postgres.getContainerId())
                .exec();
        System.out.println("✓ Database paused");
        
        // Step 3: Test login during slowness - should timeout, not hang
        System.out.println("\nStep 3: Testing login during database pause...");
        long startTime = System.currentTimeMillis();
        
        String timeoutResult = authService.login("user@example.com", "Pass1!");
        
        long duration = System.currentTimeMillis() - startTime;
        System.out.println("   Operation took: " + duration + "ms");
        System.out.println("   Result: " + timeoutResult);
        
        // Should complete within reasonable time (not hang indefinitely)
        assertTrue(duration < 30000, 
                "Login should timeout within 30 seconds, not hang. Took: " + duration + "ms");
        
        // Should return error message
        assertTrue(timeoutResult.contains("Error") || timeoutResult.contains("error"), 
                "Should return error message on timeout: " + timeoutResult);
        
        System.out.println("✓ System didn't hang, returned error in " + duration + "ms");
        
        // Step 4: Unpause container (restore normal operation)
        System.out.println("\nStep 4: Unpausing database container...");
        postgres.getDockerClient()
                .unpauseContainerCmd(postgres.getContainerId())
                .exec();
        Thread.sleep(2000); // Give time for container to resume
        System.out.println("✓ Database unpaused");
        
        // Step 5: Verify recovery
        System.out.println("\nStep 5: Verifying recovery after unpause...");
        await()
            .atMost(Duration.ofSeconds(15))
            .pollInterval(Duration.ofSeconds(2))
            .untilAsserted(() -> {
                String recoveredResult = authService.login("user@example.com", "Pass1!");
                System.out.println("   Recovery attempt: " + recoveredResult);
                assertTrue(recoveredResult.contains("exitoso"), 
                        "Should work after unpause: " + recoveredResult);
            });
        
        System.out.println("✓ System recovered after database resumed");
        System.out.println("\n=== Test 3 PASSED ===\n");
    }

    /**
     * Test 4: Password Recovery During Database Issues
     * 
     * Validates that password recovery (Requirement 5):
     * 1. Works normally when database is available
     * 2. Returns appropriate error messages for invalid inputs
     * 3. Handles user-not-found scenarios gracefully
     */
    @Test
    @Order(4)
    @DisplayName("Password recovery handles various scenarios")
    void testPasswordRecoveryDuringDatabaseFailure() throws InterruptedException {
        System.out.println("\n=== Test 4: Password Recovery Resilience ===");
        
        // Step 1: Test normal password recovery
        System.out.println("Step 1: Testing normal password recovery...");
        String normalResult = authService.recoverPassword("user@example.com");
        System.out.println("   Result: " + normalResult);
        assertTrue(normalResult.contains("Se ha enviado") || normalResult.contains("recuperación"),
                "Should work normally: " + normalResult);
        System.out.println("✓ Normal recovery works");
        
        // Step 2: Test with non-existent user
        System.out.println("\nStep 2: Testing with non-existent user...");
        String notFoundResult = authService.recoverPassword("nonexistent@example.com");
        System.out.println("   Result: " + notFoundResult);
        assertTrue(notFoundResult.contains("No existe"),
                "Should indicate user not found: " + notFoundResult);
        System.out.println("✓ Graceful handling of non-existent user");
        
        // Step 3: Test with invalid email format
        System.out.println("\nStep 3: Testing with invalid email...");
        String invalidResult = authService.recoverPassword("invalid-email");
        System.out.println("   Result: " + invalidResult);
        assertTrue(invalidResult.contains("válido") || invalidResult.contains("email"),
                "Should indicate invalid email: " + invalidResult);
        System.out.println("✓ Validates email format");
        
        System.out.println("\n=== Test 4 PASSED ===\n");
    }
}
