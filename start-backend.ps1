# Configure Java 21 environment dynamically
$env:JAVA_HOME = "C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot"
$env:PATH = "$env:JAVA_HOME\bin;$env:PATH"

Write-Host "Configuring Java environment..." -ForegroundColor Green
Write-Host "JAVA_HOME: $env:JAVA_HOME" -ForegroundColor Cyan
java -version

Write-Host "Starting Medora AI Spring Boot Backend using Local Maven..." -ForegroundColor Green
cd medora-backend
& ".\apache-maven-3.9.6\bin\mvn.cmd" spring-boot:run
