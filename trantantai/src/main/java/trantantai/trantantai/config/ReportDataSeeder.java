package trantantai.trantantai.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.constants.PaymentMethod;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Category;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.entities.ItemInvoice;
import trantantai.trantantai.entities.User;
import trantantai.trantantai.repositories.IBookRepository;
import trantantai.trantantai.repositories.ICategoryRepository;
import trantantai.trantantai.repositories.IInvoiceRepository;
import trantantai.trantantai.repositories.IUserRepository;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Seeds mock data for reports testing.
 * Creates categories, books, users, and invoices with realistic data.
 */
@Component
@Order(3) // Run after DataInitializer and UserRoleMigration
public class ReportDataSeeder implements CommandLineRunner {

    private final ICategoryRepository categoryRepository;
    private final IBookRepository bookRepository;
    private final IUserRepository userRepository;
    private final IInvoiceRepository invoiceRepository;

    public ReportDataSeeder(ICategoryRepository categoryRepository,
                            IBookRepository bookRepository,
                            IUserRepository userRepository,
                            IInvoiceRepository invoiceRepository) {
        this.categoryRepository = categoryRepository;
        this.bookRepository = bookRepository;
        this.userRepository = userRepository;
        this.invoiceRepository = invoiceRepository;
    }

    @Override
    public void run(String... args) {
        // Check if we should seed data
        long existingInvoices = invoiceRepository.count();
        long existingBooks = bookRepository.findAll().size();

        System.out.println(">>> ReportDataSeeder: Found " + existingInvoices + " invoices, " + existingBooks + " books");

        // Skip if already have substantial data
        if (existingInvoices > 100) {
            System.out.println(">>> Report mock data already exists (" + existingInvoices + " invoices), skipping seeding.");
            return;
        }

        System.out.println(">>> Seeding mock data for reports...");

        // Create categories
        List<Category> categories = createCategories();
        System.out.println(">>> Categories ready: " + categories.size());

        // Create books
        List<Book> books = createBooks(categories);
        System.out.println(">>> Books ready: " + books.size());

        // Get existing users or create test users
        List<User> users = getOrCreateUsers();
        System.out.println(">>> Users ready: " + users.size());

        // Create invoices spanning the last 12 months
        createInvoices(books, users);

        System.out.println(">>> Mock data seeding completed!");
        System.out.println(">>> Created " + categoryRepository.count() + " categories");
        System.out.println(">>> Created " + bookRepository.count() + " books");
        System.out.println(">>> Created " + invoiceRepository.count() + " invoices");
    }

    private List<Category> createCategories() {
        List<Category> existing = categoryRepository.findAll();
        if (existing.size() >= 6) {
            return existing;
        }

        String[] categoryNames = {
            "Công nghệ thông tin",
            "Kinh tế - Kinh doanh",
            "Văn học",
            "Kỹ năng sống",
            "Thiếu nhi",
            "Khoa học"
        };

        List<Category> categories = new ArrayList<>();
        for (String name : categoryNames) {
            Category cat = new Category();
            cat.setName(name);
            categories.add(categoryRepository.save(cat));
        }
        return categories;
    }

    private List<Book> createBooks(List<Category> categories) {
        List<Book> existing = bookRepository.findAll();
        if (existing.size() >= 20) {
            return existing;
        }

        // Book data: title, author, price, categoryIndex
        Object[][] bookData = {
            // Công nghệ thông tin
            {"Clean Code", "Robert C. Martin", 350000.0, 0},
            {"Design Patterns", "Gang of Four", 420000.0, 0},
            {"Refactoring", "Martin Fowler", 380000.0, 0},
            {"The Pragmatic Programmer", "David Thomas", 290000.0, 0},
            {"Java Hiệu Quả", "Joshua Bloch", 320000.0, 0},

            // Kinh tế - Kinh doanh
            {"Đắc Nhân Tâm", "Dale Carnegie", 150000.0, 1},
            {"Cha Giàu Cha Nghèo", "Robert Kiyosaki", 180000.0, 1},
            {"Tư Duy Nhanh Và Chậm", "Daniel Kahneman", 250000.0, 1},
            {"Khởi Nghiệp Tinh Gọn", "Eric Ries", 220000.0, 1},

            // Văn học
            {"Nhà Giả Kim", "Paulo Coelho", 120000.0, 2},
            {"Tuổi Trẻ Đáng Giá Bao Nhiêu", "Rosie Nguyễn", 98000.0, 2},
            {"Dám Bị Ghét", "Kishimi Ichiro", 135000.0, 2},
            {"Số Đỏ", "Vũ Trọng Phụng", 85000.0, 2},

            // Kỹ năng sống
            {"7 Thói Quen Hiệu Quả", "Stephen Covey", 195000.0, 3},
            {"Sức Mạnh Tiềm Thức", "Joseph Murphy", 145000.0, 3},
            {"Đời Ngắn Đừng Ngủ Dài", "Robin Sharma", 125000.0, 3},

            // Thiếu nhi
            {"Dế Mèn Phiêu Lưu Ký", "Tô Hoài", 65000.0, 4},
            {"Hoàng Tử Bé", "Antoine de Saint-Exupéry", 78000.0, 4},
            {"Harry Potter Tập 1", "J.K. Rowling", 185000.0, 4},

            // Khoa học
            {"Lược Sử Thời Gian", "Stephen Hawking", 175000.0, 5},
            {"Sapiens: Lược Sử Loài Người", "Yuval Noah Harari", 235000.0, 5}
        };

        List<Book> books = new ArrayList<>();
        for (Object[] data : bookData) {
            Book book = new Book();
            book.setTitle((String) data[0]);
            book.setAuthor((String) data[1]);
            book.setPrice((Double) data[2]);
            book.setQuantity(ThreadLocalRandom.current().nextInt(50, 200));

            int catIndex = (int) data[3];
            if (catIndex < categories.size()) {
                book.setCategoryId(categories.get(catIndex).getId());
            }

            books.add(bookRepository.save(book));
        }
        return books;
    }

    private List<User> getOrCreateUsers() {
        List<User> users = userRepository.findAll();
        if (users.size() >= 5) {
            return users;
        }
        // If fewer than 5 users, just return existing ones
        // We'll use the admin user for invoices if needed
        return users;
    }

    private void createInvoices(List<Book> books, List<User> users) {
        if (books.isEmpty() || users.isEmpty()) {
            System.out.println(">>> No books or users available for invoice creation");
            return;
        }

        Random random = new Random(42); // Fixed seed for reproducible data
        Calendar cal = Calendar.getInstance();

        // Create invoices for the last 12 months
        for (int monthsAgo = 11; monthsAgo >= 0; monthsAgo--) {
            // Set to first day of the month
            cal.setTime(new Date());
            cal.add(Calendar.MONTH, -monthsAgo);
            cal.set(Calendar.DAY_OF_MONTH, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);

            // Number of invoices increases over time (more recent = more sales)
            int baseOrders = 15 + (12 - monthsAgo) * 3;
            int numOrders = baseOrders + random.nextInt(10);

            int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);

            for (int i = 0; i < numOrders; i++) {
                // Random day within the month
                int day = 1 + random.nextInt(daysInMonth);
                cal.set(Calendar.DAY_OF_MONTH, Math.min(day, daysInMonth));
                cal.set(Calendar.HOUR_OF_DAY, 8 + random.nextInt(12));
                cal.set(Calendar.MINUTE, random.nextInt(60));

                Invoice invoice = new Invoice();
                invoice.setInvoiceDate(cal.getTime());
                invoice.setUserId(users.get(random.nextInt(users.size())).getId());

                // Random number of items (1-4)
                int numItems = 1 + random.nextInt(4);
                List<ItemInvoice> items = new ArrayList<>();
                double totalPrice = 0;

                Set<Integer> usedBookIndices = new HashSet<>();
                for (int j = 0; j < numItems; j++) {
                    int bookIndex;
                    do {
                        bookIndex = random.nextInt(books.size());
                    } while (usedBookIndices.contains(bookIndex) && usedBookIndices.size() < books.size());
                    usedBookIndices.add(bookIndex);

                    Book book = books.get(bookIndex);
                    int quantity = 1 + random.nextInt(3);

                    ItemInvoice item = new ItemInvoice();
                    item.setId(UUID.randomUUID().toString());
                    item.setBookId(book.getId());
                    item.setQuantity(quantity);
                    items.add(item);

                    totalPrice += book.getPrice() * quantity;
                }

                invoice.setItemInvoices(items);
                invoice.setPrice(totalPrice);

                // 85% delivered & paid, 10% processing, 5% cancelled
                int statusRoll = random.nextInt(100);
                if (statusRoll < 85) {
                    invoice.setOrderStatus(OrderStatus.DELIVERED);
                    invoice.setPaymentStatus(PaymentStatus.PAID);
                } else if (statusRoll < 95) {
                    invoice.setOrderStatus(OrderStatus.PROCESSING);
                    invoice.setPaymentStatus(PaymentStatus.COD_PENDING);
                } else {
                    invoice.setOrderStatus(OrderStatus.CANCELLED);
                    invoice.setPaymentStatus(PaymentStatus.PAYMENT_FAILED);
                }

                // 70% COD, 30% MOMO
                invoice.setPaymentMethod(random.nextInt(100) < 70 ? PaymentMethod.COD : PaymentMethod.MOMO);

                invoiceRepository.save(invoice);
            }
        }
    }
}
