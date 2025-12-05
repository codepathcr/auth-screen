# Run All Tests Script
# This script runs all unit tests, snapshot tests, and generates coverage reports

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Running All Tests for Auth-Screen" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Set Java Home
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"
Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Yellow

# Clean previous build artifacts
Write-Host ""
Write-Host "Step 1: Cleaning previous build..." -ForegroundColor Green
mvn clean
if ($LASTEXITCODE -ne 0) {
    Write-Host "Clean failed!" -ForegroundColor Red
    exit 1
}

# Compile the project
Write-Host ""
Write-Host "Step 2: Compiling project..." -ForegroundColor Green
mvn compile test-compile
if ($LASTEXITCODE -ne 0) {
    Write-Host "Compilation failed!" -ForegroundColor Red
    exit 1
}

# Run unit tests (excluding UI snapshot tests and resilience tests that require Docker)
Write-Host ""
Write-Host "Step 3: Running unit tests..." -ForegroundColor Green
mvn test -Dtest='!AuthFrameSnapshotTest,!AuthServiceResilienceTest'
$unitTestExitCode = $LASTEXITCODE
if ($unitTestExitCode -ne 0) {
    Write-Host "Unit tests failed!" -ForegroundColor Yellow
}

# Run snapshot tests separately
Write-Host ""
Write-Host "Step 4: Running snapshot tests..." -ForegroundColor Green
mvn test -Dtest='AuthFrameSnapshotTest'
$snapshotTestExitCode = $LASTEXITCODE
if ($snapshotTestExitCode -ne 0) {
    Write-Host "Snapshot tests failed!" -ForegroundColor Yellow
}

# Set overall test exit code
if ($unitTestExitCode -ne 0 -or $snapshotTestExitCode -ne 0) {
    $testExitCode = 1
} else {
    $testExitCode = 0
}

# Generate JaCoCo coverage report
Write-Host ""
Write-Host "Step 5: Generating coverage reports..." -ForegroundColor Green
mvn jacoco:report
if ($LASTEXITCODE -ne 0) {
    Write-Host "Coverage report generation failed!" -ForegroundColor Red
    exit 1
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "Test Execution Summary" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
if ($unitTestExitCode -eq 0) {
    Write-Host "  Unit Tests: PASSED" -ForegroundColor Green
} else {
    Write-Host "  Unit Tests: FAILED" -ForegroundColor Red
}
if ($snapshotTestExitCode -eq 0) {
    Write-Host "  Snapshot Tests: PASSED" -ForegroundColor Green
} else {
    Write-Host "  Snapshot Tests: FAILED" -ForegroundColor Red
}
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""
if ($testExitCode -eq 0) {
    Write-Host "All Tests Completed Successfully!" -ForegroundColor Green
} else {
    Write-Host "Some Tests Failed - Check details above" -ForegroundColor Yellow
}
Write-Host ""
Write-Host "Reports generated:" -ForegroundColor Yellow
Write-Host "  - Unit Test Reports: target/surefire-reports/" -ForegroundColor White
Write-Host "  - Coverage Report: target/site/jacoco/index.html" -ForegroundColor White
Write-Host ""
Write-Host "Opening coverage report..." -ForegroundColor Cyan
$reportPath = Join-Path $PSScriptRoot "target\site\jacoco\index.html"
if (Test-Path $reportPath) {
    Start-Process $reportPath
    if ($testExitCode -ne 0) {
        Write-Host ""
        Write-Host "Note: Some tests failed. Check the report for details." -ForegroundColor Yellow
    }
} else {
    Write-Host "Coverage report not found at: $reportPath" -ForegroundColor Yellow
    Write-Host "This may be because no tests were executed." -ForegroundColor Yellow
}

exit $testExitCode