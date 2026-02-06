# Draft: Swagger API Documentation Update

## Requirements (confirmed)
- Add Swagger annotations to WishlistApiController (7 endpoints)
- Add Swagger annotations to MoMoController (1 endpoint)
- Update application.properties to include `/admin/api/**` paths
- Update OpenApiConfig description to cover all APIs

## Technical Decisions
- **Security Annotations**: NO - keep consistent with existing controllers (no @SecurityRequirement)
- **Detail Level**: STANDARD - follow BookApiController patterns (@Tag, @Operation, @ApiResponse, @Parameter)
- **OpenApiConfig Update**: YES - expand description to mention all API groups
- **Verification Method**: Build only (`./mvnw compile`)

## Files to Modify
1. `trantantai/src/main/java/trantantai/trantantai/controllers/WishlistApiController.java`
2. `trantantai/src/main/java/trantantai/trantantai/controllers/MoMoController.java`
3. `trantantai/src/main/resources/application.properties`
4. `trantantai/src/main/java/trantantai/trantantai/config/OpenApiConfig.java`

## Pattern Reference (from BookApiController)
```java
@Tag(name = "Books", description = "Book management APIs - CRUD operations for books")
@RestController
@RequestMapping("/api/v1/books")
public class BookApiController {

    @Operation(summary = "Get all books", description = "Retrieves a paginated list of all books")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved book list")
    @GetMapping
    public ResponseEntity<List<BookGetVm>> getAllBooks(...) { ... }

    @Operation(summary = "Get book by ID", description = "Retrieves a specific book by its unique identifier")
    @ApiResponses({
        @ApiResponse(responseCode = "200", description = "Book found"),
        @ApiResponse(responseCode = "404", description = "Book not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<BookGetVm> getBookById(@Parameter(description = "Book ID", required = true) @PathVariable String id) { ... }
}
```

## Scope Boundaries
- **INCLUDE**: 
  - WishlistApiController Swagger annotations
  - MoMoController Swagger annotations  
  - application.properties path update
  - OpenApiConfig description update
- **EXCLUDE**:
  - Security annotations (@SecurityRequirement)
  - @Schema annotations for response objects
  - MVC-only controllers (CartController, BookController etc.)
  - Any endpoint not under /api/** or /admin/api/**

## Open Questions
- None - all clarified

## Interview Answers
- Q1: Security Annotations → B) Không
- Q2: Detail Level → B) Standard (như BookApiController)
- Q3: OpenApiConfig → A) Có - cập nhật description
- Q4: Verification → B) Build only
