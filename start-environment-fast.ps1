$ErrorActionPreference = "Stop"

$ScriptDir = Split-Path -Parent -Path $MyInvocation.MyCommand.Definition
if ($ScriptDir) {
    Set-Location -Path $ScriptDir
}

$env:DOCKER_BUILDKIT = "1"
$env:COMPOSE_DOCKER_CLI_BUILD = "1"

Write-Host "1. Building frontend and backend in parallel..."
$FrontendJob = Start-Job -Name "FrontendBuild" -ScriptBlock {
    Set-Location -Path "$using:ScriptDir/frontend"
    npm run build
    if ($LASTEXITCODE -ne 0) { throw "NPM build failed" }
}
$BackendJob = Start-Job -Name "BackendBuild" -ScriptBlock {
    Set-Location -Path $using:ScriptDir
    mvn package -DskipTests
    if ($LASTEXITCODE -ne 0) { throw "Maven build failed" }
}

$Jobs = @($FrontendJob, $BackendJob)
while ($true) {
    $Running = $false
    foreach ($Job in $Jobs) {
        $State = $Job.State
        if ($State -eq "Failed") {
            Write-Error "Task $($Job.Name) failed! Terminating other builds..."
            $Jobs | Stop-Job
            $prev = $ErrorActionPreference
            $ErrorActionPreference = "Continue"
            $Jobs | Receive-Job
            $ErrorActionPreference = $prev
            throw "Local build failed."
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

Write-Host "2. Starting Docker containers with local pre-built assets..."
docker compose -f docker-compose.local.yml up --build -d

Write-Host "Done! The application environment is now running."
