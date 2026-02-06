package trantantai.trantantai.services;

import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.*;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.stereotype.Service;
import trantantai.trantantai.constants.OrderStatus;
import trantantai.trantantai.constants.PaymentStatus;
import trantantai.trantantai.entities.Book;
import trantantai.trantantai.entities.Invoice;
import trantantai.trantantai.viewmodels.*;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Service for generating reports and statistics.
 * Revenue is calculated only from DELIVERED orders with PAID status.
 * Cost is estimated at 70% of revenue.
 */
@Service
public class ReportService {

    private static final Logger logger = Logger.getLogger(ReportService.class.getName());
    private static final double COST_RATIO = 0.70;
    private static final double PROFIT_RATIO = 0.30;

    private final MongoTemplate mongoTemplate;
    private final CategoryService categoryService;
    private final BookService bookService;

    @Autowired
    public ReportService(MongoTemplate mongoTemplate, CategoryService categoryService, BookService bookService) {
        this.mongoTemplate = mongoTemplate;
        this.categoryService = categoryService;
        this.bookService = bookService;
    }

    /**
     * Get date range from range string.
     * @param range today, week, month, quarter, year, custom
     * @param customStart custom start date (required if range is "custom")
     * @param customEnd custom end date (required if range is "custom")
     * @return Date array [startDate, endDate]
     */
    public Date[] getDateRange(String range, Date customStart, Date customEnd) {
        LocalDate now = LocalDate.now();
        LocalDate start;
        LocalDate end = now;

        switch (range.toLowerCase()) {
            case "today":
                start = now;
                break;
            case "week":
                start = now.minusDays(7);
                break;
            case "month":
                start = now.minusDays(30);
                break;
            case "quarter":
                start = now.minusMonths(3);
                break;
            case "year":
                start = now.minusYears(1);
                break;
            case "custom":
                if (customStart != null && customEnd != null) {
                    return new Date[]{customStart, customEnd};
                }
                start = now.minusDays(30);
                break;
            default:
                start = now.minusDays(30);
        }

        return new Date[]{
            Date.from(start.atStartOfDay(ZoneId.systemDefault()).toInstant()),
            Date.from(end.atTime(LocalTime.MAX).atZone(ZoneId.systemDefault()).toInstant())
        };
    }

    /**
     * Get previous period date range (for growth calculation).
     */
    public Date[] getPreviousPeriodRange(Date startDate, Date endDate) {
        long durationMillis = endDate.getTime() - startDate.getTime();
        Date prevEnd = new Date(startDate.getTime() - 1);
        Date prevStart = new Date(prevEnd.getTime() - durationMillis);
        return new Date[]{prevStart, prevEnd};
    }

    /**
     * Get complete overview data.
     */
    public ReportOverviewVm getOverview(String range, Date startDate, Date endDate) {
        // Current period
        Double totalRevenue = calculateTotalRevenue(startDate, endDate);
        Long totalOrders = countOrders(startDate, endDate);
        Double avgOrderValue = calculateAvgOrderValue(startDate, endDate);
        Integer newCustomers = countNewCustomers(startDate, endDate);

        // Previous period for growth calculation
        Date[] prevPeriod = getPreviousPeriodRange(startDate, endDate);
        Double prevRevenue = calculateTotalRevenue(prevPeriod[0], prevPeriod[1]);
        Long prevOrders = countOrders(prevPeriod[0], prevPeriod[1]);
        Double prevAvgValue = calculateAvgOrderValue(prevPeriod[0], prevPeriod[1]);
        Integer prevCustomers = countNewCustomers(prevPeriod[0], prevPeriod[1]);

        return new ReportOverviewVm(
            totalRevenue,
            totalOrders,
            avgOrderValue,
            newCustomers,
            calculateGrowth(totalRevenue, prevRevenue),
            calculateGrowth(totalOrders != null ? totalOrders.doubleValue() : 0, prevOrders != null ? prevOrders.doubleValue() : 0),
            calculateGrowth(avgOrderValue, prevAvgValue),
            calculateGrowth(newCustomers != null ? newCustomers.doubleValue() : 0, prevCustomers != null ? prevCustomers.doubleValue() : 0)
        );
    }

    /**
     * Calculate total revenue from DELIVERED + PAID orders.
     */
    public Double calculateTotalRevenue(Date startDate, Date endDate) {
        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        GroupOperation group = Aggregation.group().sum("price").as("totalRevenue");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();

        if (result == null) return 0.0;
        Object value = result.get("totalRevenue");
        return value != null ? ((Number) value).doubleValue() : 0.0;
    }

    /**
     * Count orders in range (only DELIVERED + PAID).
     */
    public Long countOrders(Date startDate, Date endDate) {
        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        GroupOperation group = Aggregation.group().count().as("totalOrders");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();

        if (result == null) return 0L;
        Object value = result.get("totalOrders");
        return value != null ? ((Number) value).longValue() : 0L;
    }

    /**
     * Calculate average order value.
     */
    public Double calculateAvgOrderValue(Date startDate, Date endDate) {
        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        GroupOperation group = Aggregation.group().avg("price").as("avgValue");

        Aggregation aggregation = Aggregation.newAggregation(match, group);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();

        if (result == null) return 0.0;
        Object value = result.get("avgValue");
        return value != null ? ((Number) value).doubleValue() : 0.0;
    }

    /**
     * Count new customers (users with their first order in the date range).
     */
    public Integer countNewCustomers(Date startDate, Date endDate) {
        // First, get all users who made their first order in the date range
        // We do this by finding users whose first order date falls within the range

        // Aggregation to find first order date per user
        MatchOperation matchDelivered = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
        );

        GroupOperation groupByUser = Aggregation.group("userId")
            .min("invoiceDate").as("firstOrderDate");

        MatchOperation matchInRange = Aggregation.match(
            Criteria.where("firstOrderDate").gte(startDate).lte(endDate)
        );

        GroupOperation countUsers = Aggregation.group().count().as("newCustomers");

        Aggregation aggregation = Aggregation.newAggregation(
            matchDelivered, groupByUser, matchInRange, countUsers
        );

        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
        Document result = results.getUniqueMappedResult();

        if (result == null) return 0;
        Object value = result.get("newCustomers");
        return value != null ? ((Number) value).intValue() : 0;
    }

    /**
     * Calculate growth percentage.
     */
    public Double calculateGrowth(Double current, Double previous) {
        if (previous == null || previous == 0) {
            return current != null && current > 0 ? 100.0 : 0.0;
        }
        if (current == null) current = 0.0;
        return Math.round(((current - previous) / previous) * 100 * 10) / 10.0;
    }

    /**
     * Get revenue chart data by time period.
     */
    public RevenueChartVm getRevenueChart(String range, Date startDate, Date endDate) {
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();

        // Determine grouping based on range
        String groupFormat = determineGroupFormat(range);

        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        // Group by date using dateToString
        AggregationOperation project = Aggregation.project("price", "invoiceDate")
            .and(DateOperators.DateToString.dateOf("invoiceDate").toString(groupFormat)).as("period");

        GroupOperation group = Aggregation.group("period")
            .sum("price").as("revenue")
            .count().as("orderCount");

        SortOperation sort = Aggregation.sort(Sort.Direction.ASC, "_id");

        Aggregation aggregation = Aggregation.newAggregation(match, project, group, sort);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        for (Document doc : results.getMappedResults()) {
            String period = doc.getString("_id");
            Double revenue = doc.get("revenue") != null ? ((Number) doc.get("revenue")).doubleValue() : 0.0;
            labels.add(formatPeriodLabel(period, range));
            data.add(revenue);
        }

        // Fill empty data if no results
        if (labels.isEmpty()) {
            labels.add("N/A");
            data.add(0.0);
        }

        return new RevenueChartVm(labels, data);
    }

    /**
     * Determine MongoDB date format based on range.
     */
    private String determineGroupFormat(String range) {
        switch (range.toLowerCase()) {
            case "today":
                return "%H:00"; // Group by hour
            case "week":
                return "%Y-%m-%d"; // Group by day
            case "month":
                return "%Y-%m-%d"; // Group by day
            case "quarter":
                return "%Y-%U"; // Group by week
            case "year":
                return "%Y-%m"; // Group by month
            default:
                return "%Y-%m-%d";
        }
    }

    /**
     * Format period label for display.
     */
    private String formatPeriodLabel(String period, String range) {
        if (period == null) return "N/A";
        
        try {
            switch (range.toLowerCase()) {
                case "today":
                    return period + "h";
                case "year":
                    // Convert YYYY-MM to "Tháng MM"
                    String[] parts = period.split("-");
                    if (parts.length >= 2) {
                        return "T" + parts[1];
                    }
                    return period;
                case "quarter":
                    // Week format
                    return "W" + period.split("-")[1];
                default:
                    // Day format - convert to DD/MM
                    String[] dateParts = period.split("-");
                    if (dateParts.length >= 3) {
                        return dateParts[2] + "/" + dateParts[1];
                    }
                    return period;
            }
        } catch (Exception e) {
            return period;
        }
    }

    /**
     * Get revenue by category.
     */
    public List<CategoryRevenueVm> getCategoryRevenue(Date startDate, Date endDate) {
        // First, get all invoices in range
        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        // Unwind itemInvoices to work with individual items
        UnwindOperation unwind = Aggregation.unwind("itemInvoices");

        // Project bookId from itemInvoices
        ProjectionOperation project = Aggregation.project()
            .and("itemInvoices.bookId").as("bookId")
            .and("itemInvoices.quantity").as("quantity");

        Aggregation aggregation = Aggregation.newAggregation(match, unwind, project);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        // Calculate revenue per category
        Map<String, Double> categoryRevenueMap = new HashMap<>();
        double totalRevenue = 0;

        for (Document doc : results.getMappedResults()) {
            String bookId = doc.getString("bookId");
            int quantity = doc.getInteger("quantity", 0);

            if (bookId != null) {
                Optional<Book> bookOpt = bookService.getBookById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    double itemRevenue = book.getPrice() * quantity;
                    String categoryId = book.getCategoryId() != null ? book.getCategoryId() : "unknown";
                    categoryRevenueMap.merge(categoryId, itemRevenue, Double::sum);
                    totalRevenue += itemRevenue;
                }
            }
        }

        // Convert to CategoryRevenueVm list
        final double finalTotalRevenue = totalRevenue;
        List<CategoryRevenueVm> result = categoryRevenueMap.entrySet().stream()
            .map(entry -> {
                String categoryName = "Khác";
                if (!"unknown".equals(entry.getKey())) {
                    categoryName = categoryService.getCategoryById(entry.getKey())
                        .map(cat -> cat.getName())
                        .orElse("Khác");
                }
                double percentage = finalTotalRevenue > 0 
                    ? Math.round((entry.getValue() / finalTotalRevenue) * 100 * 10) / 10.0 
                    : 0;
                return new CategoryRevenueVm(entry.getKey(), categoryName, entry.getValue(), percentage);
            })
            .sorted((a, b) -> Double.compare(b.revenue(), a.revenue()))
            .limit(6)
            .collect(Collectors.toList());

        return result;
    }

    /**
     * Get sales trend comparing current vs previous period.
     */
    public SalesTrendVm getSalesTrend(String range, Date startDate, Date endDate) {
        // Get current period data
        RevenueChartVm currentChart = getRevenueChart(range, startDate, endDate);

        // Get previous period data
        Date[] prevPeriod = getPreviousPeriodRange(startDate, endDate);
        RevenueChartVm prevChart = getRevenueChart(range, prevPeriod[0], prevPeriod[1]);

        // Use labels from current period, pad previous if needed
        List<String> labels = currentChart.labels();
        List<Double> currentData = currentChart.data();
        List<Double> prevData = new ArrayList<>(prevChart.data());

        // Pad previous data to match current length
        while (prevData.size() < currentData.size()) {
            prevData.add(0, 0.0);
        }
        // Trim if previous is longer
        if (prevData.size() > currentData.size()) {
            prevData = prevData.subList(prevData.size() - currentData.size(), prevData.size());
        }

        return new SalesTrendVm(labels, currentData, prevData);
    }

    /**
     * Get top selling books.
     */
    public List<BookSalesVm> getTopSellingBooks(Date startDate, Date endDate, int limit) {
        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        UnwindOperation unwind = Aggregation.unwind("itemInvoices");

        GroupOperation group = Aggregation.group("itemInvoices.bookId")
            .sum("itemInvoices.quantity").as("soldCount");

        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "soldCount");
        LimitOperation limitOp = Aggregation.limit(limit);

        Aggregation aggregation = Aggregation.newAggregation(match, unwind, group, sort, limitOp);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        List<BookSalesVm> topBooks = new ArrayList<>();
        for (Document doc : results.getMappedResults()) {
            String bookId = doc.getString("_id");
            int soldCount = doc.getInteger("soldCount", 0);

            if (bookId != null) {
                Optional<Book> bookOpt = bookService.getBookById(bookId);
                if (bookOpt.isPresent()) {
                    Book book = bookOpt.get();
                    double revenue = book.getPrice() * soldCount;
                    String imageUrl = book.getImageUrls() != null && !book.getImageUrls().isEmpty() 
                        ? book.getImageUrls().get(0) 
                        : null;
                    topBooks.add(new BookSalesVm(
                        bookId,
                        book.getTitle(),
                        book.getAuthor(),
                        soldCount,
                        revenue,
                        imageUrl
                    ));
                }
            }
        }

        return topBooks;
    }

    /**
     * Get revenue table with period grouping.
     */
    public List<RevenueTableRowVm> getRevenueTable(String groupBy, Date startDate, Date endDate) {
        String groupFormat = getGroupFormatForTable(groupBy);

        MatchOperation match = Aggregation.match(
            Criteria.where("orderStatus").is(OrderStatus.DELIVERED)
                .and("paymentStatus").is(PaymentStatus.PAID)
                .and("invoiceDate").gte(startDate).lte(endDate)
        );

        AggregationOperation project = Aggregation.project("price", "invoiceDate")
            .and(DateOperators.DateToString.dateOf("invoiceDate").toString(groupFormat)).as("period");

        GroupOperation group = Aggregation.group("period")
            .sum("price").as("revenue")
            .count().as("orderCount");

        SortOperation sort = Aggregation.sort(Sort.Direction.DESC, "_id");

        Aggregation aggregation = Aggregation.newAggregation(match, project, group, sort);
        AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);

        List<RevenueTableRowVm> rows = new ArrayList<>();
        Double prevRevenue = null;

        List<Document> resultList = new ArrayList<>(results.getMappedResults());
        // Reverse for growth calculation (oldest first)
        Collections.reverse(resultList);

        for (Document doc : resultList) {
            String period = doc.getString("_id");
            Double revenue = doc.get("revenue") != null ? ((Number) doc.get("revenue")).doubleValue() : 0.0;
            Long orderCount = doc.get("orderCount") != null ? ((Number) doc.get("orderCount")).longValue() : 0L;

            Double cost = revenue * COST_RATIO;
            Double profit = revenue * PROFIT_RATIO;
            Double growth = calculateGrowth(revenue, prevRevenue);

            rows.add(new RevenueTableRowVm(
                period,
                formatTablePeriodLabel(period, groupBy),
                orderCount,
                revenue,
                cost,
                profit,
                growth
            ));

            prevRevenue = revenue;
        }

        // Reverse back to newest first
        Collections.reverse(rows);
        return rows;
    }

    private String getGroupFormatForTable(String groupBy) {
        switch (groupBy.toLowerCase()) {
            case "day":
                return "%Y-%m-%d";
            case "week":
                return "%Y-W%U";
            case "month":
            default:
                return "%Y-%m";
        }
    }

    private String formatTablePeriodLabel(String period, String groupBy) {
        if (period == null) return "N/A";

        try {
            switch (groupBy.toLowerCase()) {
                case "day":
                    String[] dayParts = period.split("-");
                    if (dayParts.length >= 3) {
                        return dayParts[2] + "/" + dayParts[1] + "/" + dayParts[0];
                    }
                    return period;
                case "week":
                    return "Tuần " + period.split("-W")[1] + "/" + period.split("-W")[0];
                case "month":
                default:
                    String[] monthParts = period.split("-");
                    if (monthParts.length >= 2) {
                        return "Tháng " + monthParts[1] + "/" + monthParts[0];
                    }
                    return period;
            }
        } catch (Exception e) {
            return period;
        }
    }

    /**
     * Get order status distribution (all orders, not just delivered).
     */
    public Map<String, Long> getOrderStatusDistribution(Date startDate, Date endDate) {
        Map<String, Long> distribution = new LinkedHashMap<>();

        // Count each status
        for (OrderStatus status : OrderStatus.values()) {
            MatchOperation match = Aggregation.match(
                Criteria.where("orderStatus").is(status)
                    .and("invoiceDate").gte(startDate).lte(endDate)
            );

            GroupOperation group = Aggregation.group().count().as("count");

            Aggregation aggregation = Aggregation.newAggregation(match, group);
            AggregationResults<Document> results = mongoTemplate.aggregate(aggregation, Invoice.class, Document.class);
            Document result = results.getUniqueMappedResult();

            long count = 0;
            if (result != null && result.get("count") != null) {
                count = ((Number) result.get("count")).longValue();
            }
            distribution.put(status.name(), count);
        }

        return distribution;
    }
}
