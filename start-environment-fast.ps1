$ErrorActionPreference = "Stop"

Write-Host "1. Building frontend locally..."
Set-Location -Path "frontend"
npm run build
Set-Location -Path ".."

Write-Host "2. Building backend jar locally..."
mvn package -DskipTests

Write-Host "3. Starting Docker containers with local pre-built assets..."
docker compose -f docker-compose.local.yml up --build -d

Write-Host "Done! The application environment is now running."
