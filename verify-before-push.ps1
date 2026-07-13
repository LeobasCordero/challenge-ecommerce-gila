$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
if ($ScriptDir) {
    Set-Location -Path $ScriptDir
}

Write-Host "Starting parallel validation checks..."

$FrontendJob = Start-Job -Name "FrontendValidation" -ScriptBlock {
    Set-Location -Path "$using:ScriptDir/frontend"
    npm ci --prefer-offline
    if ($LASTEXITCODE -ne 0) { throw "npm ci failed" }
    npm run lint
    if ($LASTEXITCODE -ne 0) { throw "npm run lint failed" }
    npm run stylelint
    if ($LASTEXITCODE -ne 0) { throw "npm run stylelint failed" }
    npm run test -- --watch=false --browsers=ChromeHeadless --no-progress
    if ($LASTEXITCODE -ne 0) { throw "npm run test failed" }
    npm run test:pact
    if ($LASTEXITCODE -ne 0) { throw "npm run test:pact failed" }
}

$BackendJob = Start-Job -Name "BackendValidation" -ScriptBlock {
    Set-Location -Path $using:ScriptDir
    mvn clean verify
    if ($LASTEXITCODE -ne 0) { throw "mvn clean verify failed" }
}

$Jobs = @($FrontendJob, $BackendJob)
while ($true) {
    $Running = $false
    foreach ($Job in $Jobs) {
        $State = $Job.State
        if ($State -eq "Failed") {
            Write-Error "Validation task $($Job.Name) failed! Terminating remaining processes..."
            $Jobs | Stop-Job
            $prev = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            $Jobs | Receive-Job
            $ErrorActionPreference = $prev
            throw "Validation failed."
        }
        if ($State -eq "Running" -or $State -eq "NotStarted") {
            $Running = $true
        }
    }
    if (-not $Running) { break }
    Start-Sleep -Milliseconds 250
}

$prevEAP = $ErrorActionPreference
$ErrorActionPreference = "Continue"
$Jobs | Receive-Job -Wait | Out-Null
$ErrorActionPreference = $prevEAP

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
