/**
 * JMH (Java Microbenchmark Harness) performance benchmarks for auth-screen components.
 * 
 * <p>This package contains microbenchmarks for measuring the performance of:
 * <ul>
 *   <li>Email validation</li>
 *   <li>Password validation</li>
 *   <li>Authentication service operations</li>
 *   <li>Database connection and query performance</li>
 * </ul>
 * 
 * <h2>Running Benchmarks</h2>
 * <p>To run all benchmarks:</p>
 * <pre>
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" -Dexec.classpathScope=test
 * </pre>
 * 
 * <p>To run a specific benchmark class:</p>
 * <pre>
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" 
 *     -Dexec.classpathScope=test -Dexec.args="EmailValidatorBenchmark"
 * </pre>
 * 
 * <p>To run with custom parameters (e.g., more iterations):</p>
 * <pre>
 * mvn clean test-compile exec:java -Dexec.mainClass="org.openjdk.jmh.Main" 
 *     -Dexec.classpathScope=test -Dexec.args="-wi 5 -i 10 -f 2"
 * </pre>
 * 
 * <h2>Understanding Results</h2>
 * <ul>
 *   <li><strong>Throughput mode:</strong> Higher is better (ops/microsecond or ops/millisecond)</li>
 *   <li><strong>Score:</strong> Mean operations per time unit</li>
 *   <li><strong>Error:</strong> 99.9% confidence interval</li>
 * </ul>
 * 
 * @since 1.0.0
 */
package com.auth.benchmarks;
