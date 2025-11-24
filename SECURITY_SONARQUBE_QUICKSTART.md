# SonarQube Security Analysis - Quick Start Guide

This guide provides step-by-step instructions to set up and run SonarQube code quality and security analysis for the auth-screen project.

## Prerequisites

- Docker Desktop installed and running
- Maven installed (mvn command available)
- Java 21 (JDK 21.0.9 or later)
- Git bash or PowerShell terminal

## 1. Run SonarQube Server with Docker

Start a SonarQube container on your local machine:

```bash
docker run -d --name sonarqube -p 9000:9000 sonarqube:latest
```

**Command breakdown:**
- `docker run` - Creates and starts a new container
- `-d` - Runs container in detached mode (background)
- `--name sonarqube` - Names the container "sonarqube"
- `-p 9000:9000` - Maps port 9000 from container to host
- `sonarqube:latest` - Uses the latest SonarQube image

Wait 2-3 minutes for SonarQube to start completely.

## 2. Access SonarQube Web Interface

1. Open your browser and navigate to: **http://localhost:9000**
2. Default credentials:
   - **Username:** `admin`
   - **Password:** `admin`
3. Change the password when prompted (required on first login)

## 3. Create a New Project

1. Click **"Create Project"** → **"Manually"**
2. Fill in project details:
   - **Project display name:** Auth Screen
   - **Project key:** `auth-screen`
   - Click **"Next"**
3. Select **"Use the global setting"** for baseline
4. Click **"Create project"**

## 4. Generate Analysis Token

1. Select **"Locally"** as the analysis method
2. Generate a token:
   - **Token name:** `auth-screen-analysis` (or any name you prefer)
   - Click **"Generate"**
3. **Important:** Copy and save the token immediately (it won't be shown again)
   - Token format: `sqp_xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx`

## 5. Configure Environment Variable

Set the token as an environment variable:

### PowerShell:
```powershell
$env:SONAR_TOKEN="your-token-here"
```

### Git Bash:
```bash
export SONAR_TOKEN="your-token-here"
```

**Verification:**
```powershell
# PowerShell
echo "Token set: $($env:SONAR_TOKEN.Substring(0,10))..."

# Git Bash
echo "Token set: ${SONAR_TOKEN:0:10}..."
```

## 6. Configure Maven Project

The `pom.xml` should include SonarQube properties and plugins:

### Required Properties:
```xml
<properties>
    <sonar.projectKey>auth-screen</sonar.projectKey>
    <sonar.host.url>http://localhost:9000</sonar.host.url>
    <sonar.coverage.jacoco.xmlReportPaths>${project.build.directory}/site/jacoco/jacoco.xml</sonar.coverage.jacoco.xmlReportPaths>
    <sonar.exclusions>**/App.java,**/AuthFrame.java</sonar.exclusions>
</properties>
```

### Required Plugins:
```xml
<plugin>
    <groupId>org.sonarsource.scanner.maven</groupId>
    <artifactId>sonar-maven-plugin</artifactId>
    <version>4.0.0.4121</version>
</plugin>
```

### JaCoCo Plugin Configuration:
```xml
<plugin>
    <groupId>org.jacoco</groupId>
    <artifactId>jacoco-maven-plugin</artifactId>
    <version>0.8.10</version>
    <executions>
        <execution>
            <goals>
                <goal>prepare-agent</goal>
            </goals>
        </execution>
        <execution>
            <id>report</id>
            <phase>verify</phase>
            <goals>
                <goal>report</goal>
            </goals>
        </execution>
    </executions>
</plugin>
```

## 7. Configure Database Environment Variables

SonarQube security rules require that database credentials are NOT hardcoded. Set these environment variables:

### PowerShell:
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pswe06"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your-password-here"
```

### Git Bash:
```bash
export DB_URL="jdbc:postgresql://localhost:5432/pswe06"
export DB_USER="postgres"
export DB_PASSWORD="your-password-here"
```

**Note:** These values should match your PostgreSQL configuration.

## 8. Run SonarQube Analysis

Execute the following Maven command:

```bash
mvn clean verify sonar:sonar "-Dsonar.token=$env:SONAR_TOKEN"
```

**Command breakdown:**
- `mvn clean` - Removes previous build artifacts
- `verify` - Runs tests and generates coverage reports
- `sonar:sonar` - Executes SonarQube analysis
- `-Dsonar.token=$env:SONAR_TOKEN` - Passes authentication token

**Expected output:**
```
[INFO] ANALYSIS SUCCESSFUL, you can find the results at: http://localhost:9000/dashboard?id=auth-screen
[INFO] BUILD SUCCESS
```

## 9. View Analysis Results

1. Open the dashboard: **http://localhost:9000/dashboard?id=auth-screen**
2. Review the following metrics:
   - **Bugs** - Code reliability issues
   - **Vulnerabilities** - Security issues
   - **Code Smells** - Maintainability issues
   - **Coverage** - Test coverage percentage
   - **Duplications** - Duplicated code blocks
   - **Security Hotspots** - Security-sensitive code to review

## 10. Docker Container Management

### View running containers:
```bash
docker ps
```

### Stop SonarQube:
```bash
docker stop sonarqube
```

### Start SonarQube:
```bash
docker start sonarqube
```

### Remove container:
```bash
docker rm sonarqube
```

## Common Issues and Solutions

### Issue: Authentication Failed
**Error:** `Not authorized. Analyzing this project requires authentication`

**Solution:**
1. Verify token is set: `echo $env:SONAR_TOKEN` (PowerShell) or `echo $SONAR_TOKEN` (Bash)
2. Regenerate token in SonarQube UI: My Account → Security → Generate Token
3. Update environment variable with new token

### Issue: Test Failures Due to Missing DB Environment Variables
**Error:** `Required environment variable DB_URL is not set`

**Solution:**
Set all three database environment variables before running Maven:
```powershell
$env:DB_URL="jdbc:postgresql://localhost:5432/pswe06"
$env:DB_USER="postgres"
$env:DB_PASSWORD="your-password"
```

### Issue: SonarQube Not Accessible
**Error:** Browser shows "Unable to connect" at http://localhost:9000

**Solution:**
1. Check if container is running: `docker ps`
2. Check container logs: `docker logs sonarqube`
3. Wait 2-3 minutes for SonarQube to fully start
4. Restart container: `docker restart sonarqube`

### Issue: JaCoCo Coverage Not Showing
**Error:** Coverage shows 0% in SonarQube

**Solution:**
1. Verify JaCoCo XML report exists: `target/site/jacoco/jacoco.xml`
2. Check `sonar.coverage.jacoco.xmlReportPaths` property in pom.xml
3. Run `mvn clean verify` before `sonar:sonar`

## Security Best Practices Applied

This project follows these security practices:

1. ✅ **No hardcoded credentials** - Database credentials read from environment variables
2. ✅ **Token-based authentication** - SonarQube access uses secure tokens
3. ✅ **Environment separation** - Production and test databases use different configurations
4. ✅ **Security scanning** - Regular SonarQube analysis for vulnerability detection
5. ✅ **Code quality gates** - Automated quality checks on each analysis

## Integration with CI/CD

To integrate SonarQube analysis in a CI/CD pipeline:

1. Store `SONAR_TOKEN` as a secret in your CI/CD platform
2. Set database credentials as environment variables/secrets
3. Add the Maven command to your build pipeline
4. Configure quality gates in SonarQube to fail builds if criteria not met

### Example GitHub Actions:
```yaml
- name: Run SonarQube Analysis
  env:
    SONAR_TOKEN: ${{ secrets.SONAR_TOKEN }}
    DB_URL: ${{ secrets.DB_URL }}
    DB_USER: ${{ secrets.DB_USER }}
    DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
  run: mvn clean verify sonar:sonar -Dsonar.token=$SONAR_TOKEN
```

## Additional Resources

- [SonarQube Documentation](https://docs.sonarqube.org/latest/)
- [Maven SonarQube Plugin](https://docs.sonarqube.org/latest/analyzing-source-code/scanners/sonarscanner-for-maven/)
- [JaCoCo Code Coverage](https://www.jacoco.org/jacoco/trunk/doc/)
- [Docker SonarQube Image](https://hub.docker.com/_/sonarqube)

## Project Information

- **SonarQube Version:** 25.11.0.114957
- **SonarQube Maven Plugin:** 4.0.0.4121
- **JaCoCo Version:** 0.8.10
- **Java Version:** 21 (JDK 21.0.9)
- **Last Updated:** November 23, 2025
