@echo off
echo ================================================
echo Book Cover Upload Script
echo ================================================
echo.
echo Make sure Spring Boot app is running first!
echo Run: mvnw spring-boot:run
echo.
echo Press any key to start upload...
pause > nul

powershell -ExecutionPolicy Bypass -File "%~dp0upload-book-covers.ps1"

pause
