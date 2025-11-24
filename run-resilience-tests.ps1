# Run Resilience Tests
# This script executes only the resilience tests for the authentication system

Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Resilience Testing - Auth System  " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Prerequisites Check:" -ForegroundColor Yellow
Write-Host "  ✓ Docker Desktop must be running" -ForegroundColor Green
Write-Host "  ✓ PostgreSQL will be started in Docker" -ForegroundColor Green
Write-Host "  ✓ Database environment variables: Using Testcontainers" -ForegroundColor Green
Write-Host ""

# Check if Docker is running
Write-Host "Checking Docker..." -ForegroundColor Yellow
$dockerRunning = docker info 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Docker is not running!" -ForegroundColor Red
    Write-Host "Please start Docker Desktop and try again." -ForegroundColor Red
    exit 1
}
Write-Host "✓ Docker is running" -ForegroundColor Green
Write-Host ""

# Clean previous build
Write-Host "Cleaning previous build..." -ForegroundColor Yellow
mvn clean | Out-Null
Write-Host "✓ Build cleaned" -ForegroundColor Green
Write-Host ""

# Compile the project
Write-Host "Compiling project..." -ForegroundColor Yellow
mvn compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Compilation successful" -ForegroundColor Green
Write-Host ""

# Compile test classes
Write-Host "Compiling test classes..." -ForegroundColor Yellow
mvn test-compile -q
if ($LASTEXITCODE -ne 0) {
    Write-Host "ERROR: Test compilation failed!" -ForegroundColor Red
    exit 1
}
Write-Host "✓ Test compilation successful" -ForegroundColor Green
Write-Host ""

# Run resilience tests
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Running Resilience Tests          " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

Write-Host "Note: These tests will:" -ForegroundColor Yellow
Write-Host "  1. Start PostgreSQL in Docker" -ForegroundColor White
Write-Host "  2. Stop/Start database to test recovery" -ForegroundColor White
Write-Host "  3. Pause database to test timeouts" -ForegroundColor White
Write-Host "  4. Take approximately 2-3 minutes" -ForegroundColor White
Write-Host ""

$startTime = Get-Date

# Run the resilience test class
mvn test -Dtest=AuthServiceResilienceTest

$exitCode = $LASTEXITCODE
$endTime = Get-Date
$duration = $endTime - $startTime

Write-Host ""
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host "  Test Results                      " -ForegroundColor Cyan
Write-Host "=====================================" -ForegroundColor Cyan
Write-Host ""

if ($exitCode -eq 0) {
    Write-Host "✓ ALL RESILIENCE TESTS PASSED" -ForegroundColor Green
    Write-Host ""
    Write-Host "Tests completed in: $($duration.TotalSeconds) seconds" -ForegroundColor Green
    Write-Host ""
    Write-Host "Results:" -ForegroundColor Yellow
    Write-Host "  ✓ Database restart recovery: PASSED" -ForegroundColor Green
    Write-Host "  ✓ Failed attempts persistence: PASSED" -ForegroundColor Green
    Write-Host "  ✓ Connection timeout handling: PASSED" -ForegroundColor Green
    Write-Host "  ✓ Password recovery resilience: PASSED" -ForegroundColor Green
} else {
    Write-Host "✗ SOME TESTS FAILED" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the output above for details." -ForegroundColor Yellow
    Write-Host "Test duration: $($duration.TotalSeconds) seconds" -ForegroundColor Yellow
}

Write-Host ""
Write-Host "Detailed results: target/surefire-reports/" -ForegroundColor Cyan
Write-Host ""

exit $exitCode
