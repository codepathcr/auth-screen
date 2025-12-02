# Configuration for running the authentication application
# Update these values to match your PostgreSQL setup

$env:DB_URL = "jdbc:postgresql://localhost:5432/pswe06"
$env:DB_USER = "postgres"
$env:DB_PASSWORD = "postgres"
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.8.9-hotspot"

Write-Host "Starting authentication application..." -ForegroundColor Green
Write-Host "Java Home: $env:JAVA_HOME" -ForegroundColor Cyan
Write-Host "Database: $env:DB_URL" -ForegroundColor Cyan
Write-Host "User: $env:DB_USER" -ForegroundColor Cyan

java -cp "target/classes;lib/*" com.auth.App
