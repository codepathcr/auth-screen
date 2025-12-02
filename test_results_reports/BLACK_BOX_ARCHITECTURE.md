# Black Box Testing Report - Auth Screen Project

## Executive Summary

This document provides comprehensive information about the black box testing strategy, implementation, and results for the **auth-screen** authentication application. The project employs multiple testing layers including unit tests, integration tests, UI tests, and visual snapshot tests to ensure quality and reliability.

**Project:** auth-screen  
**Version:** 1.0.0  
**Test Framework:** JUnit 4 & JUnit 5  
**Build Tool:** Apache Maven  
**Java Version:** 21  
**Last Updated:** December 2, 2025

---

## Table of Contents

1. [Testing Technologies](#testing-technologies)
2. [Test Categories](#test-categories)
3. [How to Run Tests](#how-to-run-tests)
4. [Test Coverage](#test-coverage)
5. [Unit Tests](#unit-tests)
6. [Integration Tests](#integration-tests)
7. [UI Tests](#ui-tests)
8. [Visual Snapshot Tests](#visual-snapshot-tests)
9. [Performance Tests](#performance-tests)
10. [Test Reports](#test-reports)

---

## Testing Technologies

### Core Testing Frameworks
- **JUnit 4.11** - Primary unit testing framework
- **JUnit 5.10.1** - Used for integration tests and newer test features
- **Mockito 5.5.0** - Mocking framework for isolating dependencies

### Integration Testing
- **Testcontainers 1.19.3** - Docker-based integration testing with real PostgreSQL instances
- **PostgreSQL Testcontainer** - Containerized PostgreSQL for realistic database testing
- **H2 Database 2.2.220** - In-memory database for fast unit tests

### UI Testing
- **AssertJ-Swing** - Swing UI testing framework for automated GUI testing
- **Visual Snapshot Testing** - Custom image comparison for UI regression detection

### Code Quality & Coverage
- **JaCoCo 0.8.10** - Code coverage analysis and reporting
- **SonarQube** - Static code analysis and quality metrics
- **Awaitility 4.2.0** - Asynchronous test utilities for resilience testing

### Performance Testing
- **JMH 1.37** - Java Microbenchmark Harness for performance testing

---

## Test Categories

### 1. Unit Tests (35+ test classes)
Black box unit tests focus on verifying individual component behavior without knowledge of internal implementation:

#### Validation Tests
- `EmailValidatorTest` - Email format validation (RFC-compliant patterns)
- `PasswordValidatorTest` - Password strength validation rules

#### Authentication Service Tests
- `AuthServiceTest` - Login and password recovery workflows
- `AuthServiceBlockedUserTest` - Account blocking after failed attempts
- `AuthServiceBranchCoverageTest` - Edge cases and boundary conditions
- Multiple exception handling tests for database failures

#### Database Connection Tests
- `DbConnectionTest` - Connection establishment and configuration
- `DbConnectionEnvVarTest` - Environment variable handling
- `DbConnectionFailureTest` - Connection failure scenarios
- `DbConnectionCoverageTest` - Comprehensive branch coverage

#### UI Component Tests
- `AuthFrameTest` - Frame initialization and component presence
- `AuthFramePaintTest` - UI rendering verification

### 2. Integration Tests
- **Location:** `src/integration-test/java/com/auth/`
- **Technology:** Testcontainers with PostgreSQL
- **Scope:** End-to-end database workflows with real PostgreSQL containers

### 3. UI Tests
- **Framework:** AssertJ-Swing
- **Coverage:**
  - Login button functionality
  - Password recovery button
  - Input validation UI feedback
  - Empty field handling
  - Enter key submission

### 4. Visual Snapshot Tests
- **Framework:** Custom BufferedImage comparison
- **Purpose:** Detect visual regressions in UI rendering
- **Tolerance:** Configurable pixel difference threshold (default: 1%)

---

## How to Run Tests

### Prerequisites
```powershell
# Set JAVA_HOME
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# Verify setup
java -version
mvn -version
```

### Run All Unit Tests
```powershell
mvn test
```

### Run Specific Test Class
```powershell
mvn -Dtest=EmailValidatorTest test
```

### Run Tests with Coverage Report
```powershell
mvn clean test jacoco:report
```
Coverage report available at: `target/site/jacoco/index.html`

### Run Integration Tests
```powershell
# Requires Docker running
mvn verify
```
Integration tests execute during the `verify` phase via Maven Failsafe plugin.

### Run UI Tests
```powershell
# Run all UI tests
mvn -Dtest=AuthFrameUiTest test

# Run specific UI test
mvn -Dtest=AuthFrameUiMoreTests test
```

### Generate Visual Snapshot Baseline
```powershell
mvn -DcreateSnapshot=true -Dtest=AuthFrameSnapshotTest test
```

### Verify Visual Snapshot (Compare with Baseline)
```powershell
mvn -Dtest=AuthFrameSnapshotTest test
```
If snapshots differ, actual and diff images are saved to `target/snapshots/`

### Run Performance Benchmarks
```powershell
mvn clean test-compile exec:java
```

---

## Test Coverage

### Coverage Tool: JaCoCo

**Target Coverage:** 100% for core business logic (`com.auth` package)

**Exclusions:**
- `App.java` - Main entry point (minimal logic)
- `AuthFrame.java` - UI class (tested via UI tests, not unit coverage)

### Generate Coverage Report
```powershell
mvn clean test jacoco:report
```

### View Coverage Report
Open: `target/site/jacoco/index.html`

### Coverage Metrics Tracked
- **Line Coverage** - Percentage of executable lines executed
- **Branch Coverage** - Percentage of decision branches taken
- **Method Coverage** - Percentage of methods invoked
- **Class Coverage** - Percentage of classes loaded

### SonarQube Integration
```powershell
# Start SonarQube locally (default: http://localhost:9000)
# Then run:
mvn clean verify sonar:sonar
```

**SonarQube Configuration (pom.xml):**
- Project Key: `auth-screen`
- Coverage Plugin: JaCoCo
- XML Report Path: `target/site/jacoco/jacoco.xml`

---

## Unit Tests

### Test Philosophy
Unit tests treat components as **black boxes**, verifying:
- **Inputs:** Valid, invalid, edge cases, and boundary values
- **Outputs:** Expected return values and exceptions
- **Side Effects:** State changes, database updates, UI feedback

### Key Test Patterns

#### 1. Input Validation Tests
Test both valid and invalid inputs without knowing validation implementation:

**Example:** `EmailValidatorTest`
- Valid emails: `user@example.com`, `test.user@domain.co.uk`
- Invalid emails: `missing@domain`, `no-at-sign.com`, `@nodomain.com`
- Edge cases: empty string, null, whitespace

#### 2. State Transition Tests
Verify behavior changes based on application state:

**Example:** `AuthServiceBlockedUserTest`
- User starts unblocked
- After 5 failed login attempts, account becomes blocked
- Blocked accounts reject login attempts
- Successful login resets failed attempt counter

#### 3. Exception Handling Tests
Ensure proper error handling for failure scenarios:

**Example:** `AuthServiceSQLExceptionTest`
- Database connection failures return error messages
- SQL exceptions are caught and reported to users
- Resources are properly closed even on exceptions

#### 4. Boundary Value Tests
Test at the edges of valid input ranges:

**Example:** `PasswordValidatorTest`
- Minimum length: 5 characters
- Maximum length: 10 characters
- Required characters: uppercase, special character

### Mock Usage
Mockito isolates units under test:
- Mock database connections to test without real database
- Mock JDBC PreparedStatement and ResultSet
- Verify interactions (e.g., correct SQL executed)

---

## Integration Tests

### Technology: Testcontainers

**Purpose:** Test complete workflows with real PostgreSQL database running in Docker containers.

### Test Location
```
src/integration-test/java/com/auth/AuthServiceIT.java
```

### What Integration Tests Verify
- **Real Database Interactions:** Actual JDBC connections to PostgreSQL
- **SQL Execution:** Table creation, inserts, updates, queries
- **Transaction Behavior:** Commit/rollback scenarios
- **Data Persistence:** Verify data survives across operations

### Running Integration Tests
```powershell
# Requires Docker Desktop running
mvn verify
```

**Maven Plugins Used:**
- **maven-failsafe-plugin** - Runs `*IT.java` tests during `verify` phase
- **build-helper-maven-plugin** - Adds `src/integration-test/java` as test source

### Example Integration Test Flow
1. Testcontainer starts PostgreSQL Docker container
2. Creates `usuarios` table using SQL schema
3. Inserts test user: `ituser@local.com`
4. Calls `AuthService.loginWithConnection()` with real connection
5. Verifies login success and password recovery flows
6. Container automatically destroyed after tests

---

## UI Tests

### Framework: AssertJ-Swing

**Purpose:** Automated black box testing of Swing GUI components.

### Test Classes
- `AuthFrameUiTest` - Basic button click and text verification
- `AuthFrameUiMoreTests` - Advanced scenarios (validation, empty fields, keyboard input)

### What UI Tests Verify

#### Input Validation Feedback
- Invalid email shows "Email no válido"
- Empty fields show appropriate error messages
- Password format errors display correctly

#### User Interactions
- Login button click triggers authentication
- "Forgot password" button triggers recovery flow
- Enter key in password field submits form

#### UI State Changes
- Status label updates with success/error messages
- Component enable/disable states
- Focus handling

### Test Structure
```java
// Create frame with stubbed AuthService (no real DB)
AuthFrame frame = new AuthFrame(stubbedService);

// Use FrameFixture to interact with UI
FrameFixture window = new FrameFixture(robot(), frame);
window.show();

// Enter text in fields
window.textBox("emailField").setText("user@example.com");
window.textBox("passwordField").setText("Abc!1");

// Click button
window.button("loginButton").click();

// Verify result
window.label("statusLabel").requireText("Login exitoso 🎉");
```

### Running UI Tests
```powershell
# All UI tests
mvn test -Dtest=AuthFrameUi*

# Specific UI test
mvn -Dtest=AuthFrameUiMoreTests#enterKey_triggersLogin test
```

---

## Visual Snapshot Tests

### Technology: Custom BufferedImage Comparison

**Purpose:** Detect unintended visual changes in UI rendering (regression testing).

### How It Works

#### Baseline Creation
```powershell
mvn -DcreateSnapshot=true -Dtest=AuthFrameSnapshotTest test
```
Captures current UI rendering and saves as baseline PNG:
`src/test/resources/snapshots/AuthFrame_baseline.png`

#### Snapshot Comparison
```powershell
mvn -Dtest=AuthFrameSnapshotTest test
```
1. Renders current UI
2. Compares pixel-by-pixel with baseline
3. Calculates difference percentage
4. Fails if difference exceeds threshold (default: 1%)

#### On Failure
Creates debug images in `target/snapshots/`:
- `AuthFrame_actual.png` - Current rendering
- `AuthFrame_diff.png` - Visual diff (red = changed pixels)

### Use Cases
- Detect font rendering changes
- Catch color/styling regressions
- Verify cross-platform consistency
- Document UI appearance over time

### Considerations
- **Platform-dependent:** Fonts and antialiasing vary by OS
- **Tolerance needed:** Small pixel differences are normal
- **Baseline management:** Commit baseline images to git

---

## Performance Tests

### Framework: JMH (Java Microbenchmark Harness)

**Purpose:** Measure and track performance characteristics of critical operations.

### Running Benchmarks
```powershell
mvn clean test-compile exec:java
```

Results saved to: `target/benchmarks/results.json`

### What is Measured
- **Throughput:** Operations per second
- **Average Time:** Mean execution time
- **Sample Time:** Latency distribution
- **Single Shot:** Cold start performance

---

## Test Reports

### JaCoCo Coverage Report
**Location:** `target/site/jacoco/index.html`

**Generated by:**
```powershell
mvn clean test jacoco:report
```

**Contents:**
- Overall coverage percentages
- Per-package and per-class metrics
- Line-by-line coverage visualization
- Missed branches highlighted

### Surefire Test Reports
**Location:** `target/surefire-reports/`

**Files:**
- `TEST-*.xml` - JUnit XML test results
- `*.txt` - Plain text test output

### Failsafe Integration Test Reports
**Location:** `target/failsafe-reports/`

**Generated during:** `mvn verify`

---

## Best Practices

### 1. Test Isolation
- Each test is independent (no shared state)
- Tests can run in any order
- Use `@Before/@After` or test class setup for initialization

### 2. Descriptive Test Names
```java
// Good
invalidEmail_showsValidationMessage()

// Bad
test1()
```

### 3. Arrange-Act-Assert Pattern
```java
// Arrange
String email = "invalid-email";

// Act
String result = validator.validate(email);

// Assert
assertEquals("Email no válido", result);
```

### 4. Mock External Dependencies
- Database connections
- Network calls
- File system operations
- System clock/time

### 5. Test Data Management
- Use realistic test data
- Test edge cases and boundaries
- Include both valid and invalid inputs

---

## Continuous Integration

### Recommended CI Workflow

```yaml
# GitHub Actions example
- name: Run Unit Tests
  run: mvn test

- name: Run Integration Tests
  run: mvn verify
  # Requires Docker service

- name: Generate Coverage
  run: mvn jacoco:report

- name: Upload Coverage
  # Upload to Codecov/Coveralls
```

### Pre-commit Checks
```powershell
# Run before committing
mvn clean test
```

---

## Troubleshooting

### Common Issues

#### 1. Tests Fail Locally But Pass in CI
- Check Java version consistency
- Verify environment variables
- Ensure database is running (for integration tests)

#### 2. Integration Tests Fail
- Verify Docker is running
- Check Docker daemon is accessible
- Ensure port 5432 is available

#### 3. UI Tests Fail
- Display must be available (headless mode for CI requires Xvfb on Linux)
- Frame must be disposed properly (`DISPOSE_ON_CLOSE` not `EXIT_ON_CLOSE`)

#### 4. Coverage Report Missing Classes
- Ensure tests execute the code paths
- Check JaCoCo exclusions in `pom.xml`
- Verify test execution completes successfully

---

## Summary

The **auth-screen** project employs comprehensive black box testing:

✅ **35+ Unit Test Classes** covering validation, authentication, database, and UI logic  
✅ **Integration Tests** with Testcontainers and real PostgreSQL  
✅ **UI Tests** with AssertJ-Swing for automated GUI verification  
✅ **Visual Snapshot Tests** for UI regression detection  
✅ **100% Coverage Target** for core business logic  
✅ **JaCoCo + SonarQube** for continuous quality monitoring  
✅ **JMH Benchmarks** for performance tracking  

**Test Execution Time:**
- Unit Tests: ~10 seconds
- Integration Tests: ~20 seconds (with Testcontainers)
- Full Test Suite: ~30 seconds

**Total Test Count:** 100+ test methods across all test classes

---

## Quick Reference Commands

```powershell
# Set environment
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

# Unit tests only
mvn test

# Unit + Integration tests
mvn verify

# With coverage
mvn clean test jacoco:report

# Specific test
mvn -Dtest=EmailValidatorTest test

# Skip tests (for fast builds)
mvn clean compile -DskipTests

# UI tests
mvn -Dtest=AuthFrameUi* test

# Snapshot baseline
mvn -DcreateSnapshot=true -Dtest=AuthFrameSnapshotTest test

# Benchmarks
mvn clean test-compile exec:java
```

---

**Document Version:** 1.0  
**Generated:** December 2, 2025  
**Maintained by:** Development Team
