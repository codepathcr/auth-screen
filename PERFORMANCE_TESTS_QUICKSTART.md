# Quick Reference: Running Performance Benchmarks

## Prerequisites
```powershell
# Ensure Maven and Java are configured
mvn -v
java -version
```

## Run All Benchmarks (Results saved to JSON)
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="-rf json -rff target/benchmarks/results.json"
```
**Results location:** `target/benchmarks/results.json`

## Run By Requirement

### Requirement 3: Email Validation Performance
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="EmailValidatorBenchmark"
```

### Requirement 4: Password Validation Performance
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="PasswordValidatorBenchmark"
```

### Requirements 3-7: Complete Authentication Flow
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="AuthServiceBenchmark"
```

### Requirement 7: Database Performance
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="DbConnectionBenchmark"
```

## Run Specific Scenarios

### Requirement 6: Failed Login Attempts (5 attempts blocking)
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="AuthServiceBenchmark.testFailedLoginAttempts_5Times"
```

### Requirement 6: Account Blocking Performance
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="AuthServiceBenchmark.testAccountBlockingPerformance"
```

### Requirement 5: Password Recovery
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="AuthServiceBenchmark.testPasswordRecoveryThroughput"
```

### Attack Simulation (Brute Force)
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="AuthServiceBenchmark.testBruteForceAttackSimulation"
```

## Quick Test (Faster Results)
Reduce iterations for quick validation:
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="-wi 1 -i 2 -f 1 EmailValidatorBenchmark"
```

## Generate Results Report
Save results to JSON file:
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="-rf json -rff benchmark-results.json"
```

## Performance Profiling

### GC (Garbage Collection) Profiling
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="-prof gc AuthServiceBenchmark"
```

### Stack Profiling (Hotspot Detection)
```powershell
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test -Dexec.args="-prof stack PasswordValidatorBenchmark"
```

## Expected Performance Targets

| Component | Target | Critical Threshold |
|-----------|--------|-------------------|
| Email Validation | >1M ops/sec | <1 microsecond |
| Password Validation | >500K ops/sec | <5 microseconds |
| Successful Login | >100 ops/ms | <50ms per operation |
| Failed Login (1-5) | No degradation | Consistent across all 5 |
| Account Blocking | >100 ops/ms | <5ms blocking check |
| Password Recovery | >100 ops/ms | <10ms per operation |
| Simple DB Query | >1000 ops/ms | <1ms per query |
| Login Transaction | >100 ops/ms | <10ms per transaction |

## Reading Results

Example output:
```
Benchmark                                          Mode  Cnt     Score     Error   Units
EmailValidatorBenchmark.testValidEmail            thrpt    5  1234.567 ± 12.345  ops/us
```

- **Mode:** thrpt = Throughput (higher is better)
- **Score:** 1234.567 operations per microsecond
- **Error:** ±12.345 confidence interval (99.9%)
- **ops/us:** Operations per microsecond (1M ops/us = 1 trillion ops/sec)
- **ops/ms:** Operations per millisecond (1K ops/ms = 1M ops/sec)

## Troubleshooting

**Compilation errors:**
```powershell
mvn clean compile test-compile
```

**Memory issues:**
```powershell
$env:MAVEN_OPTS="-Xmx2g"
mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test
```

**High variance in results:**
- Close unnecessary applications
- Run with more forks: `-f 3`
- Increase iterations: `-i 10`

For detailed documentation, see `BENCHMARKS.md`
