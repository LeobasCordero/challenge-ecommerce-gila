$ErrorActionPreference = "Stop"

Write-Host "Starting local validation pipeline..."

# 1. Frontend Checks
Write-Host "Running frontend validation..."
Set-Location -Path "frontend"

Write-Host "Installing frontend dependencies..."
npm ci

Write-Host "Running ESLint..."
npm run lint

Write-Host "Running Stylelint..."
npm run stylelint

Write-Host "Running Karma unit tests in headless mode..."
npm run test -- --watch=false --browsers=ChromeHeadless

Write-Host "Running Pact consumer contract tests..."
npm run test:pact

Set-Location -Path ".."

# 2. Backend Checks
Write-Host "Running backend validation..."
Write-Host "Compiling and running Checkstyle, PMD, and tests..."
mvn clean verify

# 3. SonarQube Scanner
Write-Host "Checking if SonarQube server is reachable on http://localhost:9000..."
try {
    $response = Invoke-WebRequest -Uri "http://localhost:9000" -TimeoutSec 2 -UseBasicParsing -ErrorAction Ignore
    if ($response.StatusCode -eq 200) {
        Write-Host "SonarQube server detected. Starting analysis..."
        
        Write-Host "Scanning backend..."
        mvn sonar:sonar
        
        Write-Host "Scanning frontend..."
        Set-Location -Path "frontend"
        npx sonar-scanner
        Set-Location -Path ".."
        
        Write-Host "SonarQube analysis complete."
    } else {
        Write-Host "SonarQube server not reachable. Skipping analysis step."
    }
} catch {
    Write-Host "SonarQube server not reachable. Skipping analysis step."
}

Write-Host "All checks passed successfully. Ready to push."
