# Draft: Image Upload Feature for Books and Reviews

## User Request Summary
Implement backend image upload functionality for:
1. **Book Images (Admin)**: Admin can upload multiple images when creating/editing books
2. **Review Images (User)**: Users can upload images when submitting reviews

## Requirements (from user)

### Book Images
- Multiple images per book (gallery)
- Admin uploads when creating/editing books
- Images served publicly
- Add/remove images when editing

### Review Images
- Multiple images per review (max ~5)
- User uploads when creating review
- Images linked to review
- Images served publicly

### Technical Requirements
- Image validation: JPEG, PNG, GIF, WebP
- Max file size: 5MB
- Swagger documentation
- Spring Security integration
- Follow existing codebase patterns

## Existing Codebase Patterns Discovered

### Entity Pattern
- Uses `@Document` for MongoDB
- Validation annotations on fields
- Manual getters/setters
- Reference IDs stored as String (e.g., `categoryId`, `bookId`)

### ViewModel Pattern
- Java records with `@Schema` for Swagger
- `from()` static factory methods for entity conversion
- Validation annotations on record components

### Controller Pattern
- `@RestController` + `@RequestMapping`
- `@Tag` for Swagger grouping
- `@AuthenticationPrincipal User user` for auth
- Returns `ResponseEntity<T>`

### Service Pattern
- `@Service` annotation
- Constructor injection with `@Autowired`
- Business logic validation

### Security Pattern
- Role-based: ADMIN and USER
- `/admin/**`, `/books/add`, `/books/edit/**`, `/books/delete/**` → ADMIN
- `/api/**` → authenticated
- CSRF disabled for specific endpoints (e.g., `/api/momo/**`)

## Open Questions (to ask user)

1. **Storage Strategy**: MongoDB GridFS vs. Local Filesystem?
   - GridFS: Native MongoDB, scales with database
   - Filesystem: Simpler, but needs separate backup, static file serving config

2. **Image Upload Flow**:
   - Option A: Separate upload endpoint (returns image ID, then include in entity save)
   - Option B: Multipart form with entity data + files together
   - Which approach preferred?

3. **Image Processing**:
   - Need thumbnail generation?
   - Image compression/resizing?

4. **Review Image Timing**:
   - Upload during review creation only, or edit capability later?

5. **Existing Image Field**:
   - Does Book currently have any image URL field in database?

## Research Findings

### Codebase Exploration Results
- **Book entity**: No existing imageUrl field - needs to be added
- **No existing file upload infrastructure** - No MultipartFile handling anywhere
- **Entity pattern**: `@Document`, String IDs, Jakarta validation, manual refs
- **ViewModel pattern**: Java records, `from()` factory methods, @Schema for Swagger
- **Security**: Role-based (ADMIN/USER), CSRF disabled for webhooks (/api/momo/**)

### Librarian Research Results
- **MongoDB GridFS**: Native file storage, stores metadata, scales with DB, auto-chunks large files
- **Filesystem**: Faster for small files, easier CDN integration, needs separate backup
- **Spring Multipart Config**: `spring.servlet.multipart.max-file-size=10MB` etc.
- **Image Validation**: Check MIME type, read with ImageIO to verify valid image
- **Best Practice**: Use **separate upload endpoint** (upload → get ID → attach to entity)
- **Thumbnailator library**: Recommended for compression/thumbnail generation

### Storage Comparison
| Aspect | GridFS | Filesystem |
|--------|--------|------------|
| Scalability | ✅ Distributed | ❌ Single server |
| Backup | ✅ With MongoDB | ❌ Separate needed |
| Performance | ⚠️ Slower small files | ✅ Faster |
| CDN | ❌ Stream through app | ✅ Direct serving |
| Metadata queries | ✅ MongoDB queries | ❌ Need DB |

## Technical Decisions (CONFIRMED by user)

- **Storage mechanism**: **Cloudinary** (cloud-based image CDN)
  - API Key: 175823613897388
  - API Secret: 02Fh3kbm0xrcejIOzNP0210ZlRE
- **Upload endpoint pattern**: Separate endpoints (upload → get URL → attach to entity)
- **Image processing**: Cloudinary handles compression/optimization automatically
- **Thumbnails**: Cloudinary transformation URLs (on-the-fly)
- **Review editing**: Creation only (no image editing after review created)
- **Max review images**: 3 per review
- **Max book images**: Unlimited (gallery)

## Architecture Decision
- Upload to Cloudinary → Store returned URL in MongoDB
- No GridFS needed - URLs only in entities
- Cloudinary SDK for Java handles upload
- Cloudinary CDN serves images (fast, global)
