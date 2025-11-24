# Snyk Security Scanning - Quick Start Guide

This guide provides step-by-step instructions to set up and run Snyk security vulnerability scanning for the auth-screen project.

## What is Snyk?

Snyk is a developer-first security platform that helps you find and automatically fix vulnerabilities in your:
- **Dependencies** (open source libraries)
- **Code** (your application code)
- **Containers** (Docker images)
- **Infrastructure as Code** (Terraform, Kubernetes, etc.)

---

## Prerequisites

- **Node.js and npm** installed (for Snyk CLI)
- **Maven** installed (for Java project)
- **Project with pom.xml** (Maven-based Java project)
- **Internet connection** (for Snyk API access)

---

## 1. Install Snyk CLI

### Option 1: Using npm (Recommended)

```bash
npm install -g snyk
```

### Option 2: Using Standalone Binary

Download from: https://github.com/snyk/cli/releases

### Verify Installation

```bash
snyk --version
```

Expected output: `1.x.x` (current version)

---

## 2. Authenticate with Snyk

### Sign Up / Log In

Run the authentication command:

```bash
snyk auth
```

**What happens:**
1. Your browser will open automatically
2. You'll be redirected to Snyk's authentication page
3. Log in with your account or create a new one (supports GitHub, Google, Bitbucket)
4. After successful authentication, return to your terminal

**Authentication URL format:**
```
https://app.snyk.io/oauth2/authorize?access_type=offline&client_id=...
```

**Confirmation message:**
```
Your account has been authenticated.
```

### Authentication Token

Your authentication token is stored locally at:
- **Windows:** `%USERPROFILE%\.snyk\config.json`
- **Linux/Mac:** `~/.snyk/config.json`

**Note:** Keep this token secure. Do not commit it to version control.

---

## 3. Run Your First Scan

### Basic Scan Command

Navigate to your project directory and run:

```bash
snyk test
```

**What it does:**
- Scans your `pom.xml` for dependency vulnerabilities
- Checks against Snyk's vulnerability database
- Reports found issues with severity levels
- Suggests remediation steps

### Expected Output (Example)

```
Testing C:\Users\Vargas\Documents\GitHub\auth-screen...

Tested 2 dependencies for known issues, found 1 issue, 1 vulnerable path.

Issues to fix by upgrading:

  Upgrade org.postgresql:postgresql@42.6.0 to org.postgresql:postgresql@42.6.1 to fix
  ✗ SQL Injection [Critical Severity][https://security.snyk.io/vuln/SNYK-JAVA-ORGPOSTGRESQL-6252740]
    in org.postgresql:postgresql@42.6.0
    introduced by org.postgresql:postgresql@42.6.0

Organization:      your-org-name
Package manager:   maven
Target file:       pom.xml
Project name:      com.auth:auth-screen
```

### Exit Codes

- **0:** No vulnerabilities found
- **1:** Vulnerabilities found
- **2:** Command error

---

## 4. Understanding Scan Results

### Severity Levels

| Severity | Risk Level | Action Required |
|----------|------------|-----------------|
| **Critical** | Immediate threat | Fix immediately |
| **High** | Significant risk | Fix within 1 week |
| **Medium** | Moderate risk | Fix within 1 month |
| **Low** | Minor risk | Fix when convenient |

### Vulnerability Information

Each vulnerability report includes:
- **Vulnerability ID:** Snyk ID (e.g., SNYK-JAVA-ORGPOSTGRESQL-6252740)
- **CVE ID:** Common Vulnerabilities and Exposures identifier
- **Title:** Brief description (e.g., "SQL Injection")
- **Severity:** Critical, High, Medium, or Low
- **CVSS Score:** 0-10 numerical score
- **Affected Package:** Package name and version
- **Fix:** Recommended upgrade version
- **Vulnerability Link:** Detailed information URL

---

## 5. Advanced Scan Options

### Scan All Sub-projects

```bash
snyk test --all-projects
```

Use this for multi-module Maven projects or monorepos.

### Export Results to JSON

```bash
snyk test --json > snyk-results.json
```

Useful for:
- Automated reporting
- Integration with other tools
- Detailed analysis

### Scan Specific File

```bash
snyk test --file=pom.xml
```

### Set Severity Threshold

```bash
snyk test --severity-threshold=high
```

Only report high and critical vulnerabilities.

### Include Development Dependencies

```bash
snyk test --dev
```

Include dependencies in the test scope.

---

## 6. Continuous Monitoring

### Enable Snyk Monitoring

After fixing vulnerabilities, enable continuous monitoring:

```bash
snyk monitor
```

**What it does:**
- Takes a snapshot of your current dependencies
- Uploads to your Snyk account
- Monitors for new vulnerabilities continuously
- Sends email alerts when new issues are found

**Output:**
```
Monitoring C:\Users\Vargas\Documents\GitHub\auth-screen (com.auth:auth-screen)...

Explore this snapshot at https://app.snyk.io/org/your-org/project/...

Notifications about newly disclosed issues related to these dependencies
will be emailed to you.
```

---

## 7. Fix Vulnerabilities

### Automatic Fix (Guided)

```bash
snyk wizard
```

Interactive wizard that helps you:
1. Review vulnerabilities
2. Choose remediation options
3. Apply automatic upgrades
4. Create `.snyk` policy file for ignores

### Manual Fix - Upgrade Dependencies

Based on scan results, update your `pom.xml`:

```xml
<!-- BEFORE -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.0</version>
</dependency>

<!-- AFTER -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.1</version>
</dependency>
```

Then update dependencies:

```bash
mvn clean install
```

### Verify Fix

Run Snyk scan again:

```bash
snyk test
```

Expected output after fix:
```
✓ Tested 2 dependencies for known issues, no vulnerable paths found.
```

---

## 8. Integration with CI/CD

### GitHub Actions

Create `.github/workflows/snyk-security.yml`:

```yaml
name: Snyk Security Scan

on: [push, pull_request]

jobs:
  security:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v3
      
      - name: Run Snyk to check for vulnerabilities
        uses: snyk/actions/maven@master
        env:
          SNYK_TOKEN: ${{ secrets.SNYK_TOKEN }}
        with:
          args: --severity-threshold=high
          
      - name: Upload result to GitHub Code Scanning
        uses: github/codeql-action/upload-sarif@v2
        with:
          sarif_file: snyk.sarif
```

### Maven Plugin

Add to your `pom.xml`:

```xml
<plugin>
    <groupId>io.snyk</groupId>
    <artifactId>snyk-maven-plugin</artifactId>
    <version>2.2.0</version>
    <inherited>false</inherited>
    <executions>
        <execution>
            <id>snyk-test</id>
            <goals>
                <goal>test</goal>
            </goals>
        </execution>
        <execution>
            <id>snyk-monitor</id>
            <goals>
                <goal>monitor</goal>
            </goals>
        </execution>
    </executions>
    <configuration>
        <apiToken>${env.SNYK_TOKEN}</apiToken>
        <failOnSeverity>medium</failOnSeverity>
        <org>${env.SNYK_ORG}</org>
    </configuration>
</plugin>
```

Run with Maven:

```bash
mvn snyk:test
mvn snyk:monitor
```

### Set Environment Variable

Store your Snyk token as an environment variable:

**PowerShell:**
```powershell
$env:SNYK_TOKEN="your-token-here"
```

**Bash:**
```bash
export SNYK_TOKEN="your-token-here"
```

**Permanent (Windows System Settings):**
1. Open System Properties → Environment Variables
2. Add new variable: `SNYK_TOKEN` with your token value

---

## 9. Create Snyk Policy File

### What is .snyk Policy?

The `.snyk` file allows you to:
- Ignore specific vulnerabilities (with justification)
- Define patch strategies
- Set organization-specific rules

### Create .snyk File

Create `.snyk` in your project root:

```yaml
# Snyk (https://snyk.io) policy file
version: v1.25.1

# Ignore specific vulnerabilities
ignore:
  'SNYK-JAVA-ORGPOSTGRESQL-6252740':
    - '*':
        reason: 'Not using PreferQueryMode=SIMPLE'
        expires: '2025-12-31T00:00:00.000Z'
        created: '2025-11-23T00:00:00.000Z'

# Patches (if available)
patch: {}
```

### Ignore Options

**Temporary Ignore:**
```yaml
ignore:
  'SNYK-ID':
    - '*':
        reason: 'Investigating issue'
        expires: '2025-12-31T00:00:00.000Z'
```

**Permanent Ignore (Not Recommended):**
```yaml
ignore:
  'SNYK-ID':
    - '*':
        reason: 'False positive - verified safe'
```

**Important:** Always provide a reason and set expiration dates for ignores.

---

## 10. Best Practices

### Security Workflow

1. **Before Every Commit**
   ```bash
   snyk test
   ```

2. **Weekly Monitoring**
   ```bash
   snyk monitor
   ```

3. **Monthly Dependency Review**
   ```bash
   snyk test --all-projects
   ```

### Remediation Priority

1. **Critical Severity**
   - Fix within 24 hours
   - Block deployments until fixed

2. **High Severity**
   - Fix within 1 week
   - Schedule fix in current sprint

3. **Medium Severity**
   - Fix within 1 month
   - Add to backlog

4. **Low Severity**
   - Fix opportunistically
   - Combine with other updates

### Team Collaboration

1. **Share Scan Results**
   - Export to JSON: `snyk test --json > report.json`
   - Share Snyk dashboard link
   - Include in security reviews

2. **Document Decisions**
   - Use `.snyk` policy file for ignores
   - Add comments explaining exemptions
   - Review ignores quarterly

3. **Automate Security Checks**
   - Add to CI/CD pipeline
   - Set up automated alerts
   - Integrate with issue tracking

---

## 11. Common Commands Reference

### Basic Commands

```bash
# Authenticate
snyk auth

# Test for vulnerabilities
snyk test

# Test all sub-projects
snyk test --all-projects

# Monitor project
snyk monitor

# View help
snyk --help
```

### Advanced Commands

```bash
# Test with severity threshold
snyk test --severity-threshold=high

# Export to JSON
snyk test --json > results.json

# Test specific file
snyk test --file=pom.xml

# Ignore issues using policy
snyk ignore --id=SNYK-ID --reason="Not applicable"

# Update Snyk CLI
npm update -g snyk
```

### Information Commands

```bash
# Check Snyk version
snyk --version

# View authentication status
snyk config get api

# List organizations
snyk config get org

# View current configuration
snyk config
```

---

## 12. Troubleshooting

### Issue: Authentication Failed

**Error:**
```
Authentication failed. Please run `snyk auth`
```

**Solution:**
```bash
snyk auth
```
Complete the browser authentication flow.

---

### Issue: API Token Not Found

**Error:**
```
`snyk` requires an authenticated account. Please run `snyk auth`
```

**Solution:**
1. Check if token exists: `snyk config get api`
2. Re-authenticate: `snyk auth`
3. Set token manually:
   ```bash
   snyk config set api=YOUR_TOKEN
   ```

---

### Issue: Maven Dependencies Not Found

**Error:**
```
Could not find dependencies in pom.xml
```

**Solution:**
1. Ensure you're in project root directory
2. Run Maven install first: `mvn clean install`
3. Specify file explicitly: `snyk test --file=pom.xml`

---

### Issue: Network/Proxy Problems

**Error:**
```
Failed to fetch data from Snyk API
```

**Solution:**
```bash
# Set proxy
snyk config set proxy=http://proxy.company.com:8080

# Set timeout
snyk config set timeout=300000
```

---

### Issue: Too Many Vulnerabilities

**Strategy:**
1. Filter by severity:
   ```bash
   snyk test --severity-threshold=high
   ```

2. Focus on direct dependencies first
3. Create remediation plan in priority order
4. Use `snyk wizard` for guided fixing

---

## 13. Snyk Dashboard

### Access Your Dashboard

Visit: **https://app.snyk.io/**

### Dashboard Features

1. **Projects Overview**
   - All monitored projects
   - Vulnerability counts
   - Risk scores

2. **Vulnerability Details**
   - Detailed vulnerability information
   - Remediation advice
   - Dependency trees

3. **Reports**
   - Security reports
   - Compliance reports
   - Trend analysis

4. **Integrations**
   - GitHub/GitLab/Bitbucket
   - JIRA/Slack
   - CI/CD tools

5. **Organization Settings**
   - Team management
   - Policy configuration
   - Notification settings

---

## 14. Security Scanning Checklist

Use this checklist for regular security maintenance:

- [ ] Install and authenticate Snyk CLI
- [ ] Run initial `snyk test` scan
- [ ] Review and document all findings
- [ ] Fix critical and high severity issues
- [ ] Enable `snyk monitor` for continuous monitoring
- [ ] Add Snyk to CI/CD pipeline
- [ ] Create `.snyk` policy file if needed
- [ ] Set up email notifications
- [ ] Schedule monthly dependency reviews
- [ ] Document security processes
- [ ] Train team on Snyk usage
- [ ] Review ignored vulnerabilities quarterly

---

## 15. Additional Resources

### Official Documentation
- **Snyk Documentation:** https://docs.snyk.io/
- **Snyk CLI Reference:** https://docs.snyk.io/snyk-cli
- **Maven Integration:** https://docs.snyk.io/scan-applications/snyk-open-source/snyk-open-source-supported-languages-and-package-managers/snyk-for-java-and-kotlin

### Snyk Community
- **Community Forum:** https://community.snyk.io/
- **GitHub Issues:** https://github.com/snyk/cli/issues
- **Status Page:** https://status.snyk.io/

### Security Resources
- **Snyk Vulnerability Database:** https://security.snyk.io/
- **Snyk Learn:** https://learn.snyk.io/
- **Snyk Blog:** https://snyk.io/blog/

### Training
- **Snyk Training:** https://training.snyk.io/
- **Security Best Practices:** https://snyk.io/learn/
- **DevSecOps Guide:** https://snyk.io/learn/devsecops/

---

## 16. Quick Command Summary

```bash
# Setup
npm install -g snyk          # Install Snyk CLI
snyk auth                    # Authenticate

# Scanning
snyk test                    # Scan for vulnerabilities
snyk test --all-projects     # Scan all sub-projects
snyk test --json > report.json  # Export results

# Monitoring
snyk monitor                 # Enable continuous monitoring

# Fixing
snyk wizard                  # Interactive fix wizard
# Then manually update pom.xml and run:
mvn clean install           # Update dependencies
snyk test                   # Verify fix

# Integration
snyk config set api=TOKEN   # Set API token
```

---

## Project-Specific Configuration

### Current Project: auth-screen

**Package Manager:** Maven  
**Target File:** pom.xml  
**Dependencies Scanned:** 2  
**Organization:** vargasmiranda-victormanuel

### Initial Scan Results (November 23, 2025)

- **Vulnerabilities Found:** 1 Critical
- **Affected Package:** `org.postgresql:postgresql@42.6.0`
- **Issue:** SQL Injection (CVE-2024-1597)
- **Fix:** Upgrade to version `42.6.1`

### Remediation Applied

```xml
<!-- Updated in pom.xml -->
<dependency>
    <groupId>org.postgresql</groupId>
    <artifactId>postgresql</artifactId>
    <version>42.6.1</version>  <!-- Fixed version -->
</dependency>
```

---

## Conclusion

Snyk provides powerful security scanning capabilities with minimal setup. By following this guide, you can:

✅ Identify vulnerabilities in dependencies  
✅ Receive remediation guidance  
✅ Monitor projects continuously  
✅ Integrate security into CI/CD  
✅ Maintain secure software supply chain

**Next Steps:**
1. Run `snyk test` regularly (at least weekly)
2. Enable `snyk monitor` for automated alerts
3. Integrate into your CI/CD pipeline
4. Keep dependencies up to date
5. Review the Snyk dashboard regularly

**Remember:** Security is an ongoing process, not a one-time task. Regular scanning and timely remediation are key to maintaining a secure application.

---

**Document Version:** 1.0  
**Last Updated:** November 23, 2025  
**Maintained By:** auth-screen project team
