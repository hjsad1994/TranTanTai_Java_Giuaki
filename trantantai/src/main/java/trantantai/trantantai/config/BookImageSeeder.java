package trantantai.trantantai.config;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.repositories.IBookRepository;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Seeds book cover images from URLs to Cloudinary.
 * Runs after ReportDataSeeder to ensure books exist.
 * 
 * Uses public domain book cover images from Open Library covers API.
 */
@Component
@Order(4) // Run after ReportDataSeeder
public class BookImageSeeder implements CommandLineRunner {

    private static final Logger logger = Logger.getLogger(BookImageSeeder.class.getName());

    private final IBookRepository bookRepository;
    private final Cloudinary cloudinary;

    // Map book titles to Open Library cover URLs (ISBN-based or ID-based)
    // Using Open Library Covers API: https://covers.openlibrary.org/
    private static final Map<String, String> BOOK_COVER_URLS = new HashMap<>();

    static {
        // Công nghệ thông tin - Programming books
        BOOK_COVER_URLS.put("Clean Code", "https://covers.openlibrary.org/b/isbn/9780132350884-L.jpg");
        BOOK_COVER_URLS.put("Design Patterns", "https://covers.openlibrary.org/b/isbn/9780201633610-L.jpg");
        BOOK_COVER_URLS.put("Refactoring", "https://covers.openlibrary.org/b/isbn/9780134757599-L.jpg");
        BOOK_COVER_URLS.put("The Pragmatic Programmer", "https://covers.openlibrary.org/b/isbn/9780135957059-L.jpg");
        BOOK_COVER_URLS.put("Java Hiệu Quả", "https://covers.openlibrary.org/b/isbn/9780134685991-L.jpg"); // Effective Java

        // Kinh tế - Kinh doanh - Business books
        BOOK_COVER_URLS.put("Đắc Nhân Tâm", "https://covers.openlibrary.org/b/isbn/9780671027032-L.jpg"); // How to Win Friends
        BOOK_COVER_URLS.put("Cha Giàu Cha Nghèo", "https://covers.openlibrary.org/b/isbn/9781612680194-L.jpg"); // Rich Dad Poor Dad
        BOOK_COVER_URLS.put("Tư Duy Nhanh Và Chậm", "https://covers.openlibrary.org/b/isbn/9780374533557-L.jpg"); // Thinking Fast and Slow
        BOOK_COVER_URLS.put("Khởi Nghiệp Tinh Gọn", "https://covers.openlibrary.org/b/isbn/9780307887894-L.jpg"); // The Lean Startup

        // Văn học - Literature
        BOOK_COVER_URLS.put("Nhà Giả Kim", "https://covers.openlibrary.org/b/isbn/9780062315007-L.jpg"); // The Alchemist
        BOOK_COVER_URLS.put("Tuổi Trẻ Đáng Giá Bao Nhiêu", "https://images-na.ssl-images-amazon.com/images/I/61jw+XGREKL.jpg"); // Vietnamese book
        BOOK_COVER_URLS.put("Dám Bị Ghét", "https://covers.openlibrary.org/b/isbn/9781501197277-L.jpg"); // The Courage to Be Disliked
        BOOK_COVER_URLS.put("Số Đỏ", "https://salt.tikicdn.com/cache/w1200/ts/product/5e/18/24/2a6154ba08df6ce6161c13f4303fa19e.jpg"); // Vietnamese classic

        // Kỹ năng sống - Self-help
        BOOK_COVER_URLS.put("7 Thói Quen Hiệu Quả", "https://covers.openlibrary.org/b/isbn/9781982137274-L.jpg"); // 7 Habits
        BOOK_COVER_URLS.put("Sức Mạnh Tiềm Thức", "https://covers.openlibrary.org/b/isbn/9780735204317-L.jpg"); // Power of Subconscious Mind
        BOOK_COVER_URLS.put("Đời Ngắn Đừng Ngủ Dài", "https://covers.openlibrary.org/b/isbn/9780061173929-L.jpg"); // Who Will Cry When You Die

        // Thiếu nhi - Children
        BOOK_COVER_URLS.put("Dế Mèn Phiêu Lưu Ký", "https://salt.tikicdn.com/cache/w1200/ts/product/45/3c/fc/c742831f4e3f5c52a01a75e7ddf2d52a.jpg"); // Vietnamese classic
        BOOK_COVER_URLS.put("Hoàng Tử Bé", "https://covers.openlibrary.org/b/isbn/9780156012195-L.jpg"); // The Little Prince
        BOOK_COVER_URLS.put("Harry Potter Tập 1", "https://covers.openlibrary.org/b/isbn/9780747532743-L.jpg"); // Harry Potter 1

        // Khoa học - Science
        BOOK_COVER_URLS.put("Lược Sử Thời Gian", "https://covers.openlibrary.org/b/isbn/9780553380163-L.jpg"); // A Brief History of Time
        BOOK_COVER_URLS.put("Sapiens: Lược Sử Loài Người", "https://covers.openlibrary.org/b/isbn/9780062316097-L.jpg"); // Sapiens
    }

    public BookImageSeeder(IBookRepository bookRepository, Cloudinary cloudinary) {
        this.bookRepository = bookRepository;
        this.cloudinary = cloudinary;
    }

    @Override
    public void run(String... args) {
        System.out.println(">>> BookImageSeeder: Starting book cover image seeding...");

        List<Book> books = bookRepository.findAll();
        int updated = 0;
        int skipped = 0;
        int failed = 0;

        for (Book book : books) {
            // Skip if book already has images
            if (book.getImageUrls() != null && !book.getImageUrls().isEmpty()) {
                skipped++;
                continue;
            }

            String coverUrl = BOOK_COVER_URLS.get(book.getTitle());
            if (coverUrl == null) {
                System.out.println(">>> No cover URL mapped for: " + book.getTitle());
                continue;
            }

            try {
                String cloudinaryUrl = uploadFromUrl(coverUrl, book.getTitle());
                if (cloudinaryUrl != null) {
                    book.setImageUrls(List.of(cloudinaryUrl));
                    bookRepository.save(book);
                    updated++;
                    System.out.println(">>> Uploaded cover for: " + book.getTitle());
                } else {
                    failed++;
                }
            } catch (Exception e) {
                logger.log(Level.WARNING, "Failed to upload cover for: " + book.getTitle(), e);
                failed++;
            }

            // Small delay to avoid rate limiting
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                break;
            }
        }

        System.out.println(">>> BookImageSeeder completed!");
        System.out.println(">>> Updated: " + updated + ", Skipped (already has images): " + skipped + ", Failed: " + failed);
    }

    /**
     * Upload image from URL to Cloudinary.
     */
    @SuppressWarnings("unchecked")
    private String uploadFromUrl(String imageUrl, String bookTitle) {
        try {
            // Sanitize title for public_id
            String sanitizedTitle = bookTitle.toLowerCase()
                    .replaceAll("[^a-z0-9]+", "-")
                    .replaceAll("^-|-$", "");

            Map<String, Object> uploadResult = cloudinary.uploader().upload(
                    imageUrl,
                    ObjectUtils.asMap(
                            "folder", "books",
                            "public_id", "cover-" + sanitizedTitle,
                            "resource_type", "image",
                            "transformation", "q_auto,f_auto",
                            "overwrite", true
                    )
            );

            return (String) uploadResult.get("secure_url");
        } catch (IOException e) {
            logger.log(Level.WARNING, "Failed to upload from URL: " + imageUrl, e);
            return null;
        }
    }
}
