# Book Cover Image Upload Script - Direct Cloudinary Upload
# Uploads book covers directly to Cloudinary, then updates database via REST API
# 
# Usage: .\upload-book-covers.ps1
# Requires: Spring Boot app running at localhost:8080

$ErrorActionPreference = "Stop"

# Configuration
$BaseUrl = "http://localhost:8080"
$Username = "admin"
$Password = "admin123"

# Cloudinary Configuration (from application.properties)
$CloudName = "dshxievwb"
$ApiKey = "175823613897388"
$ApiSecret = "02Fh3kbm0xrcejIOzNP0210ZlRE"

# Book cover URLs mapping (Vietnamese title -> Open Library cover URL)
$CoverUrls = @{
    "Clean Code" = "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg"
    "Design Patterns" = "https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg"
    "Refactoring" = "https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg"
    "The Pragmatic Programmer" = "https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg"
    "Java Hieu Qua" = "https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg"
    "Dac Nhan Tam" = "https://covers.openlibrary.org/b/isbn/9780671027032-L.jpg"
    "Cha Giau Cha Ngheo" = "https://covers.openlibrary.org/b/isbn/9781612680194-L.jpg"
    "Tu Duy Nhanh Va Cham" = "https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg"
    "Khoi Nghiep Tinh Gon" = "https://covers.openlibrary.org/b/isbn/9780307887894-L.jpg"
    "Nha Gia Kim" = "https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg"
    "Dam Bi Ghet" = "https://covers.openlibrary.org/b/isbn/9781501197277-L.jpg"
    "7 Thoi Quen Hieu Qua" = "https://covers.openlibrary.org/b/isbn/9781982137274-L.jpg"
    "Suc Manh Tiem Thuc" = "https://covers.openlibrary.org/b/isbn/9780735204317-L.jpg"
    "Doi Ngan Dung Ngu Dai" = "https://covers.openlibrary.org/b/isbn/9780061173929-L.jpg"
    "Hoang Tu Be" = "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"
    "Harry Potter Tap 1" = "https://covers.openlibrary.org/b/isbn/9780747532743-L.jpg"
    "Luoc Su Thoi Gian" = "https://covers.openlibrary.org/b/isbn/9780553380163-L.jpg"
    "Sapiens: Luoc Su Loai Nguoi" = "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"
}

Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Book Cover Image Upload Script" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host ""

# Step 1: Check if app is running
Write-Host "Step 1: Checking if app is running..." -ForegroundColor Yellow
try {
    $response = Invoke-WebRequest -Uri "$BaseUrl/" -Method GET -UseBasicParsing -TimeoutSec 5
    Write-Host "  App is running!" -ForegroundColor Green
} catch {
    Write-Host "  ERROR: App is not running!" -ForegroundColor Red
    Write-Host "  Please start with: .\mvnw spring-boot:run" -ForegroundColor Red
    exit 1
}

# Step 2: Login to get session
Write-Host ""
Write-Host "Step 2: Logging in as admin..." -ForegroundColor Yellow

# Create a web session to store cookies
$session = New-Object Microsoft.PowerShell.Commands.WebRequestSession

try {
    # First get login page to get CSRF token
    $loginPage = Invoke-WebRequest -Uri "$BaseUrl/login" -Method GET -WebSession $session -UseBasicParsing
    
    # Extract CSRF token
    $csrfToken = ""
    if ($loginPage.Content -match 'name="_csrf"\s+value="([^"]+)"') {
        $csrfToken = $matches[1]
        Write-Host "  CSRF token obtained" -ForegroundColor Green
    }
    
    # Login via form POST
    $loginBody = @{
        username = $Username
        password = $Password
        _csrf = $csrfToken
    }
    
    $loginResponse = Invoke-WebRequest -Uri "$BaseUrl/login" -Method POST -Body $loginBody -WebSession $session -UseBasicParsing -MaximumRedirection 5
    Write-Host "  Login successful!" -ForegroundColor Green
} catch {
    Write-Host "  Login may have issues, continuing anyway..." -ForegroundColor Yellow
}

# Step 3: Get all books (public API)
Write-Host ""
Write-Host "Step 3: Fetching book list..." -ForegroundColor Yellow

try {
    $booksResponse = Invoke-WebRequest -Uri "$BaseUrl/api/v1/books?pageSize=100" -Method GET -WebSession $session -UseBasicParsing
    $books = $booksResponse.Content | ConvertFrom-Json
    
    if ($books -is [array]) {
        Write-Host "  Found $($books.Count) books" -ForegroundColor Green
    } else {
        Write-Host "  Found 1 book" -ForegroundColor Green
        $books = @($books)
    }
} catch {
    Write-Host "  ERROR fetching books: $_" -ForegroundColor Red
    Write-Host "  Response: $($_.Exception.Response)" -ForegroundColor Red
    exit 1
}

if ($books.Count -eq 0) {
    Write-Host "  No books found in database!" -ForegroundColor Yellow
    exit 0
}

# Step 4: Upload covers for each book
Write-Host ""
Write-Host "Step 4: Uploading book covers..." -ForegroundColor Yellow
Write-Host ""

$updated = 0
$skipped = 0
$failed = 0

# Create temp directory for downloads
$tempDir = Join-Path $env:TEMP "book-covers"
if (!(Test-Path $tempDir)) {
    New-Item -ItemType Directory -Path $tempDir | Out-Null
}

foreach ($book in $books) {
    $title = $book.title
    $bookId = $book.id
    
    if ([string]::IsNullOrEmpty($title)) {
        Write-Host "  [SKIP] Book has no title" -ForegroundColor Gray
        $skipped++
        continue
    }
    
    # Skip if book already has images
    if ($book.imageUrls -and $book.imageUrls.Count -gt 0) {
        Write-Host "  [SKIP] $title - already has image" -ForegroundColor Gray
        $skipped++
        continue
    }
    
    # Normalize title for lookup (remove Vietnamese diacritics)
    $normalizedTitle = $title
    
    # Check if we have a cover URL for this book
    $coverUrl = $null
    foreach ($key in $CoverUrls.Keys) {
        if ($title -like "*$key*" -or $key -like "*$title*" -or $title -eq $key) {
            $coverUrl = $CoverUrls[$key]
            break
        }
    }
    
    if (!$coverUrl) {
        Write-Host "  [SKIP] $title - no cover URL mapped" -ForegroundColor Gray
        $skipped++
        continue
    }
    
    Write-Host "  [UPLOAD] $title..." -ForegroundColor White -NoNewline
    
    try {
        # Upload directly to Cloudinary via unsigned upload (URL-based)
        $timestamp = [int][double]::Parse((Get-Date -UFormat %s))
        $sanitizedTitle = ($title -replace '[^a-zA-Z0-9]', '-').ToLower()
        $publicId = "books/cover-$sanitizedTitle"
        
        # Create signature for signed upload
        $signatureString = "folder=books&public_id=cover-$sanitizedTitle&timestamp=$timestamp$ApiSecret"
        $sha1 = [System.Security.Cryptography.SHA1]::Create()
        $signatureBytes = $sha1.ComputeHash([System.Text.Encoding]::UTF8.GetBytes($signatureString))
        $signature = [BitConverter]::ToString($signatureBytes).Replace("-", "").ToLower()
        
        # Upload to Cloudinary
        $uploadBody = @{
            file = $coverUrl
            api_key = $ApiKey
            timestamp = $timestamp
            public_id = "cover-$sanitizedTitle"
            folder = "books"
            signature = $signature
        }
        
        $cloudinaryResponse = Invoke-RestMethod -Uri "https://api.cloudinary.com/v1_1/$CloudName/image/upload" -Method POST -Body $uploadBody
        $imageUrl = $cloudinaryResponse.secure_url
        
        if ($imageUrl) {
            # Update book with image URL via API
            $updateBody = @{
                title = $book.title
                author = $book.author
                price = $book.price
                categoryId = $book.categoryId
                quantity = $book.quantity
                imageUrls = @($imageUrl)
            } | ConvertTo-Json -Depth 3
            
            $headers = @{
                "Content-Type" = "application/json"
            }
            
            Invoke-RestMethod -Uri "$BaseUrl/api/v1/books/$bookId" -Method PUT -Body $updateBody -Headers $headers -WebSession $session | Out-Null
            
            Write-Host " OK ($imageUrl)" -ForegroundColor Green
            $updated++
        } else {
            Write-Host " FAILED: No URL returned" -ForegroundColor Red
            $failed++
        }
        
        # Small delay to avoid rate limiting
        Start-Sleep -Milliseconds 1000
        
    } catch {
        Write-Host " FAILED: $($_.Exception.Message)" -ForegroundColor Red
        $failed++
    }
}

# Cleanup temp directory
Remove-Item $tempDir -Recurse -Force -ErrorAction SilentlyContinue

# Summary
Write-Host ""
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "Upload Complete!" -ForegroundColor Cyan
Write-Host "================================================" -ForegroundColor Cyan
Write-Host "  Updated: $updated" -ForegroundColor Green
Write-Host "  Skipped: $skipped" -ForegroundColor Yellow
Write-Host "  Failed:  $failed" -ForegroundColor Red
Write-Host ""
Write-Host "Visit http://localhost:8080 to see the book covers!" -ForegroundColor Cyan
