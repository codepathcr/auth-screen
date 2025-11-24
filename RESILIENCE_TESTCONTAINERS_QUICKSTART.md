# Resilience Testing Quickstart Guide

**Framework:** Testcontainers + JUnit 5 + Awaitility  
**Duration:** 5-10 minutes to run tests  
**Prerequisites:** Docker Desktop, Java 21, Maven

---

## What Are Resilience Tests?

Resilience tests validate your application's behavior when **dependencies fail**:
- Database crashes and restarts
- Network timeouts and slowness
- Connection pool exhaustion
- Disk full scenarios

Unlike unit tests that mock failures, resilience tests use **real containers** to simulate actual production failures.

---

## Quick Start (3 Steps)

### Step 1: Start Docker Desktop

```powershell
# Windows: Open Docker Desktop application
# Verify Docker is running:
docker ps
```

**Must see:** Docker daemon responding (even if no containers running)

### Step 2: Set JAVA_HOME

```powershell
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"
```

### Step 3: Run Tests

```powershell
mvn test -Dtest=AuthServiceResilienceTest
```

**Expected Output:**
```
[INFO] Tests run: 4, Failures: 0, Errors: 0, Skipped: 0
[INFO] BUILD SUCCESS
```

**Duration:** ~45-60 seconds

---

## What Gets Tested?

### ✅ Test 1: Database Restart Recovery (11.8s)
**Scenario:** Database crashes while users are logging in

**What Happens:**
1. PostgreSQL container starts
2. User logs in successfully ✓
3. Database stops (simulated crash)
4. Login fails with clear error message ✓
5. Database restarts automatically
6. Login works again within 3 seconds ✓

**Why It Matters:** Production databases restart for maintenance, upgrades, or crashes. System must recover gracefully.

---

### ✅ Test 2: Security Counter Persistence (1.3s)
**Scenario:** Attacker tries to bypass login attempt blocking by causing system failures

**What Happens:**
1. User has 3 failed login attempts already
2. Make 3 more wrong attempts
3. Account gets blocked at 5 attempts ✓
4. Blocking persists across restarts ✓

**Why It Matters:** Security mechanisms must survive system failures. Attackers cannot bypass blocking by causing crashes.

---

### ✅ Test 3: Connection Timeout Handling (11.9s)
**Scenario:** Database becomes slow or unresponsive

**What Happens:**
1. User logs in normally ✓
2. Database connection paused (simulated network issue)
3. Login attempt times out after 5 seconds (doesn't hang forever) ✓
4. Database resumes
5. Login works immediately ✓

**Why It Matters:** Slow databases shouldn't freeze your application. Users get clear error messages instead of infinite loading.

---

### ✅ Test 4: Password Recovery Resilience (1.1s)
**Scenario:** Password recovery function handles edge cases

**What Happens:**
1. Normal recovery works: "Email sent" ✓
2. Non-existent user: "User not found" ✓
3. Invalid email format: "Enter valid email" ✓

**Why It Matters:** Functions must validate inputs and handle errors gracefully, even during failures.

---

## Understanding the Output

### Successful Test Run
```
=== Test 1: Database Restart Recovery ===
✓ Normal login works
✓ Database stopped
✓ System detected failure gracefully
✓ Database restarted on new port
✓ System recovered in 3114ms

=== Test 1 PASSED ===
```

### Test Failure Example
```
[ERROR] testDatabaseRestartRecovery
org.opentest4j.AssertionFailedError: System should recover
==> expected: <true> but was: <false>
```

---

## Troubleshooting

### ❌ "Docker is not running"
```
ERROR: Testcontainers could not connect to Docker
```

**Solution:**
1. Open Docker Desktop
2. Wait for "Docker Desktop is running" status
3. Run tests again

---

### ❌ "Tests hang at container startup"
```
[INFO] Running com.auth.AuthServiceResilienceTest
(hangs for 2+ minutes)
```

**Possible Causes:**
- Docker Desktop just started (needs warm-up time)
- Windows VM slow startup
- Antivirus blocking Docker

**Solutions:**
1. Restart Docker Desktop completely
2. Use Alpine image (already configured: `postgres:15-alpine`)
3. Increase timeout in code: `.withStartupTimeout(Duration.ofMinutes(2))`

---

### ❌ "JAVA_HOME not set"
```
[ERROR] JAVA_HOME must be set
```

**Solution:**
```powershell
# Set for current session
$env:JAVA_HOME = "C:\Program Files\Java\jdk-21"

# Or set permanently (Windows)
[System.Environment]::SetEnvironmentVariable("JAVA_HOME", "C:\Program Files\Java\jdk-21", "User")
```

---

### ❌ "Port already in use"
```
Caused by: BindException: Address already in use
```

**Solution:**
Testcontainers uses random ports, so this is rare. If it happens:
```powershell
# Stop all containers
docker stop $(docker ps -aq)
```

---

## How Testcontainers Works

### 1. Container Lifecycle
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
    .withDatabaseName("testdb")
    .withStartupTimeout(Duration.ofMinutes(2));
```

**What Happens:**
- Container starts automatically before tests (@BeforeAll)
- Runs on random available port
- Stops automatically after all tests (@AfterAll)
- Cleaned up completely (no leftover containers)

### 2. Dynamic Configuration
```java
@BeforeEach
void setup() {
    // Get actual port assigned by Docker
    System.setProperty("DB_URL", postgres.getJdbcUrl());
    System.setProperty("DB_USER", postgres.getUsername());
    System.setProperty("DB_PASSWORD", postgres.getPassword());
}
```

**Why Random Ports:**
- Prevents conflicts with other running databases
- Allows parallel test execution
- More realistic (production uses non-standard ports)

### 3. Test Data Initialization
```java
void initializeTestData() throws SQLException {
    try (Connection conn = DriverManager.getConnection(
            postgres.getJdbcUrl(), 
            postgres.getUsername(), 
            postgres.getPassword())) {
        
        // Create schema matching production
        String createTable = "CREATE TABLE IF NOT EXISTS usuarios (...)";
        conn.createStatement().execute(createTable);
        
        // Insert test users
        PreparedStatement ps = conn.prepareStatement(
            "INSERT INTO usuarios (email, clave_hash, intentos_fallidos, bloqueado) VALUES (?, ?, ?, ?)"
        );
        // ...
    }
}
```

**Fresh Data Every Test:** Each test gets clean database state.

---

## Using Awaitility for Async Operations

### Without Awaitility (❌ Bad)
```java
postgres.start();
Thread.sleep(5000); // Hope it's enough?
String result = authService.login(email, password);
// Might fail if database not ready yet
```

### With Awaitility (✅ Good)
```java
postgres.start();

await()
    .atMost(Duration.ofSeconds(30))
    .pollInterval(Duration.ofSeconds(1))
    .untilAsserted(() -> {
        String result = authService.login(email, password);
        assertTrue(result.contains("exitoso"), "Login should work");
    });
```

**Benefits:**
- Waits up to 30 seconds (adjustable)
- Checks every 1 second (configurable)
- Fails fast if condition never met
- Clearer test intent

---

## Running Individual Tests

### Run Single Test
```powershell
mvn test -Dtest=AuthServiceResilienceTest#testDatabaseRestartRecovery
```

### Run Specific Test Numbers
```powershell
# Test 1 only
mvn test -Dtest=AuthServiceResilienceTest#testDatabaseRestartRecovery

# Test 2 only
mvn test -Dtest=AuthServiceResilienceTest#testFailedAttemptsCounterPersistenceAcrossRestart

# Test 3 only
mvn test -Dtest=AuthServiceResilienceTest#testConnectionTimeoutHandling

# Test 4 only
mvn test -Dtest=AuthServiceResilienceTest#testPasswordRecoveryDuringDatabaseFailure
```

---

## Performance Tips

### Faster Test Execution

**1. Use Alpine Images**
```java
// Standard image (slower)
new PostgreSQLContainer<>("postgres:15")

// Alpine image (faster, already configured)
new PostgreSQLContainer<>("postgres:15-alpine")
```

**Size Difference:** 
- `postgres:15` → ~350MB
- `postgres:15-alpine` → ~230MB

**2. Reuse Containers (Advanced)**
```java
@Container
static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
    .withReuse(true); // Don't recreate between test runs
```

**Warning:** Only for local development, not CI/CD.

**3. Parallel Test Execution**
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <parallel>classes</parallel>
        <threadCount>2</threadCount>
    </configuration>
</plugin>
```

---

## CI/CD Integration

### GitHub Actions Example
```yaml
name: Resilience Tests

on: [push, pull_request]

jobs:
  test:
    runs-on: ubuntu-latest
    
    steps:
    - uses: actions/checkout@v3
    
    - name: Set up JDK 21
      uses: actions/setup-java@v3
      with:
        java-version: '21'
        distribution: 'temurin'
    
    - name: Run resilience tests
      run: mvn test -Dtest=AuthServiceResilienceTest
      
    - name: Upload test reports
      if: always()
      uses: actions/upload-artifact@v3
      with:
        name: test-reports
        path: target/surefire-reports/
```

**Note:** GitHub Actions has Docker pre-installed, so tests run directly.

---

## Adding Your Own Resilience Tests

### Template for New Test
```java
@Test
@Order(5)
@DisplayName("Your test description")
void testYourScenario() throws InterruptedException {
    System.out.println("\n=== Test 5: Your Scenario ===");
    
    // Step 1: Setup
    System.out.println("Step 1: Setting up...");
    // ... your setup code
    System.out.println("✓ Setup complete");
    
    // Step 2: Simulate failure
    System.out.println("\nStep 2: Simulating failure...");
    postgres.stop(); // or postgres.getDockerClient().pauseContainerCmd(...)
    Thread.sleep(2000);
    System.out.println("✓ Failure simulated");
    
    // Step 3: Verify graceful handling
    System.out.println("\nStep 3: Verifying error handling...");
    String result = authService.yourMethod();
    assertTrue(result.contains("Error"), "Should handle failure: " + result);
    System.out.println("✓ Graceful handling confirmed");
    
    // Step 4: Recovery
    System.out.println("\nStep 4: Recovering...");
    postgres.start();
    System.setProperty("DB_URL", postgres.getJdbcUrl());
    System.out.println("✓ Database restarted");
    
    // Step 5: Verify recovery
    System.out.println("\nStep 5: Verifying recovery...");
    await()
        .atMost(Duration.ofSeconds(30))
        .pollInterval(Duration.ofSeconds(1))
        .untilAsserted(() -> {
            String recoveredResult = authService.yourMethod();
            assertTrue(recoveredResult.contains("success"));
        });
    System.out.println("✓ Recovery confirmed");
    
    System.out.println("\n=== Test 5 PASSED ===\n");
}
```

---

## Docker Commands Reference

### Useful Docker Commands During Testing

```powershell
# List running containers
docker ps

# See all containers (including stopped)
docker ps -a

# View container logs
docker logs <container-id>

# Stop all containers
docker stop $(docker ps -aq)

# Remove all stopped containers
docker rm $(docker ps -aq)

# View Docker disk usage
docker system df

# Clean up unused containers/images
docker system prune
```

---

## FAQ

### Q: Why Testcontainers instead of H2/in-memory database?

**A:** Testcontainers uses **real PostgreSQL**, catching issues that in-memory databases miss:
- PostgreSQL-specific SQL syntax
- Transaction isolation behavior
- Connection pool edge cases
- Actual timeout behavior
- Real performance characteristics

### Q: Are tests slower than unit tests?

**A:** Yes, but worth it:
- Unit tests: ~0.1s per test (mocked dependencies)
- Resilience tests: ~10s per test (real database)
- **Trade-off:** Higher confidence in production behavior

### Q: Do I need resilience tests if I have unit tests?

**A:** Yes! They test different things:
- **Unit tests:** "Does this method work correctly?"
- **Resilience tests:** "Does the system survive when dependencies fail?"

### Q: Can I run tests without Docker?

**A:** No, Testcontainers requires Docker. Alternatives:
- Use in-memory H2 database (loses realism)
- Use TestNG + embedded PostgreSQL (complex setup)
- Mock database failures (doesn't test real connections)

### Q: How much disk space do I need?

**A:** Approximately:
- Docker Desktop: ~500MB
- PostgreSQL Alpine image: ~230MB
- Test containers (temp): ~50MB per run
- **Total:** ~1GB recommended

---

## Next Steps

1. ✅ Run tests locally: `mvn test -Dtest=AuthServiceResilienceTest`
2. ✅ Review test output and understand failure scenarios
3. ✅ Read `RESILIENCE_TESTCONTAINERS_REPORT.md` for detailed analysis
4. 🔄 Add resilience tests to your CI/CD pipeline
5. 🔄 Create additional tests for your specific failure scenarios

---

## Additional Resources

- **Testcontainers Docs:** https://testcontainers.com/
- **Awaitility Guide:** https://github.com/awaitility/awaitility
- **JUnit 5 Manual:** https://junit.org/junit5/docs/current/user-guide/

---

**Questions?** Check the main test file: `src/test/java/com/auth/AuthServiceResilienceTest.java`

**Good luck testing! 🚀**
