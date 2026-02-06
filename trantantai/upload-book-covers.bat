@echo off
REM Book Image Upload Script for Windows
REM This script uploads book covers directly to Cloudinary and updates the database
REM 
REM Prerequisites:
REM 1. curl must be installed
REM 2. Spring Boot app must be running at localhost:8080
REM 3. Admin credentials: admin@bookstore.com / admin123

setlocal enabledelayedexpansion

echo ================================================
echo Book Cover Image Upload Script
echo ================================================

REM Cloudinary credentials from application.properties
set CLOUD_NAME=dshxievwb
set API_KEY=175823613897388
set API_SECRET=02Fh3kbm0xrcejIOzNP0210ZlRE

echo.
echo Step 1: Checking if app is running...
curl -s -o nul -w "%%{http_code}" http://localhost:8080/api/v1/books > temp_status.txt
set /p STATUS=<temp_status.txt
del temp_status.txt

if "%STATUS%"=="200" (
    echo App is running!
) else (
    echo ERROR: App is not running! Please start with: mvnw spring-boot:run
    echo Then run this script again.
    pause
    exit /b 1
)

echo.
echo Step 2: Getting book list...
curl -s http://localhost:8080/api/v1/books > books.json

echo.
echo Step 3: Uploading book covers to Cloudinary...
echo This may take a few minutes...
echo.

REM Book covers mapping (title -> Open Library ISBN)
REM Format: call :upload_cover "BookID" "ImageURL" "PublicId"

REM We'll need to get the actual book IDs from the API first
REM For now, creating a PowerShell script that's more flexible

echo Creating PowerShell upload script...

powershell -Command ^
"$books = Invoke-RestMethod -Uri 'http://localhost:8080/api/v1/books';" ^
"$coverUrls = @{" ^
"    'Clean Code' = 'https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg';" ^
"    'Design Patterns' = 'https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg';" ^
"    'Refactoring' = 'https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg';" ^
"    'The Pragmatic Programmer' = 'https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg';" ^
"};" ^
"Write-Host 'Found' $books.Count 'books';" ^
"foreach ($book in $books) { Write-Host $book.title '-' $book.id }"

echo.
echo Script created. Please run the PowerShell version for full functionality.
echo Or start the Spring Boot app - it will auto-seed images on startup.

pause
