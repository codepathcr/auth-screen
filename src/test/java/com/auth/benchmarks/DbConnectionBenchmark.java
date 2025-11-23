package com.auth;

import org.openjdk.jmh.annotations.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;
import java.util.concurrent.TimeUnit;

/**
 * Benchmark for database connection acquisition and query execution.
 * Tests connection pool performance and query throughput.
 */
@BenchmarkMode(Mode.Throughput)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
@State(Scope.Benchmark)
@Warmup(iterations = 2, time = 2)
@Measurement(iterations = 3, time = 3)
@Fork(1)
public class DbConnectionBenchmark {

    private Connection persistentConnection;

    @Setup(Level.Trial)
    public void setupDatabase() throws Exception {
        Class.forName("org.h2.Driver");
        persistentConnection = DriverManager.getConnection(
            "jdbc:h2:mem:perfdb;DB_CLOSE_DELAY=-1", "sa", ""
        );
        
        try (Statement stmt = persistentConnection.createStatement()) {
            stmt.execute(
                "CREATE TABLE usuarios (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "email VARCHAR(255) UNIQUE NOT NULL, " +
                "clave_hash VARCHAR(255) NOT NULL, " +
                "intentos_fallidos INT NOT NULL DEFAULT 0, " +
                "bloqueado BOOLEAN NOT NULL DEFAULT FALSE)"
            );
            
            // Insert test data
            for (int i = 0; i < 100; i++) {
                stmt.execute(String.format(
                    "INSERT INTO usuarios (email, clave_hash) VALUES ('user%d@test.com', 'Pass1!')", i
                ));
            }
        }
    }

    @TearDown(Level.Trial)
    public void tearDown() throws Exception {
        if (persistentConnection != null && !persistentConnection.isClosed()) {
            persistentConnection.close();
        }
    }

    /**
     * Benchmark connection creation overhead.
     * Measures time to establish new database connections.
     */
    @Benchmark
    public Connection testConnectionCreation() throws Exception {
        Connection conn = DriverManager.getConnection(
            "jdbc:h2:mem:perfdb;DB_CLOSE_DELAY=-1", "sa", ""
        );
        conn.close();
        return conn;
    }

    /**
     * Benchmark simple SELECT query on existing connection.
     */
    @Benchmark
    public int testSimpleQuery() throws Exception {
        try (Statement stmt = persistentConnection.createStatement();
             var rs = stmt.executeQuery("SELECT COUNT(*) FROM usuarios")) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        return 0;
    }

    /**
     * Benchmark SELECT query with WHERE clause (typical login query pattern).
     */
    @Benchmark
    public String testSelectWithWhere() throws Exception {
        try (var ps = persistentConnection.prepareStatement(
                "SELECT clave_hash FROM usuarios WHERE email = ?")) {
            ps.setString(1, "user50@test.com");
            try (var rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getString(1);
                }
            }
        }
        return null;
    }

    /**
     * Benchmark UPDATE query (failed login attempts).
     */
    @Benchmark
    public int testUpdateQuery() throws Exception {
        try (var ps = persistentConnection.prepareStatement(
                "UPDATE usuarios SET intentos_fallidos = intentos_fallidos + 1 WHERE email = ?")) {
            ps.setString(1, "user25@test.com");
            return ps.executeUpdate();
        }
    }

    /**
     * Benchmark transaction with multiple operations.
     * Simulates a complete login flow with read and write.
     */
    @Benchmark
    public boolean testTransactionFlow() throws Exception {
        persistentConnection.setAutoCommit(false);
        try {
            // Read user data
            try (var ps = persistentConnection.prepareStatement(
                    "SELECT id, intentos_fallidos FROM usuarios WHERE email = ?")) {
                ps.setString(1, "user10@test.com");
                try (var rs = ps.executeQuery()) {
                    if (!rs.next()) {
                        return false;
                    }
                    int id = rs.getInt(1);
                    
                    // Update attempts
                    try (var updatePs = persistentConnection.prepareStatement(
                            "UPDATE usuarios SET intentos_fallidos = 0 WHERE id = ?")) {
                        updatePs.setInt(1, id);
                        updatePs.executeUpdate();
                    }
                }
            }
            persistentConnection.commit();
            return true;
        } catch (Exception e) {
            persistentConnection.rollback();
            throw e;
        } finally {
            persistentConnection.setAutoCommit(true);
        }
    }
}
