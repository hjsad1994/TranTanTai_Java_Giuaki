package trantantai.trantantai.services;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.stereotype.Service;
import trantantai.trantantai.viewmodels.*;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service for exporting reports to PDF format.
 * Professional styling with header, footer, and beautiful tables.
 */
@Service
public class PdfExportService {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Brand colors
    private static final Color TEAL_PRIMARY = new Color(13, 148, 136);
    private static final Color TEAL_DARK = new Color(15, 118, 110);
    private static final Color TEAL_LIGHT = new Color(240, 253, 250);
    private static final Color CORAL = new Color(249, 115, 22);
    private static final Color SUCCESS = new Color(22, 163, 74);
    private static final Color DANGER = new Color(220, 38, 38);
    private static final Color GRAY_50 = new Color(250, 250, 249);
    private static final Color GRAY_100 = new Color(245, 245, 244);
    private static final Color GRAY_200 = new Color(231, 229, 228);
    private static final Color GRAY_500 = new Color(120, 113, 108);
    private static final Color GRAY_700 = new Color(68, 64, 60);
    private static final Color GRAY_900 = new Color(28, 25, 23);

    /**
     * Export full report to PDF.
     */
    public byte[] exportReport(
            ReportOverviewVm overview,
            List<BookSalesVm> topBooks,
            List<RevenueTableRowVm> revenueTable,
            String dateRange
    ) throws DocumentException, IOException {
        return exportReport(overview, topBooks, revenueTable, null, dateRange);
    }

    /**
     * Export full report to PDF with category data.
     */
    public byte[] exportReport(
            ReportOverviewVm overview,
            List<BookSalesVm> topBooks,
            List<RevenueTableRowVm> revenueTable,
            List<CategoryRevenueVm> categoryRevenue,
            String dateRange
    ) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // Add page event for header/footer
            writer.setPageEvent(new HeaderFooterPageEvent(dateRange));

            document.open();

            // ═══════════════════════════════════════════════════════════════════
            // COVER / TITLE SECTION
            // ═══════════════════════════════════════════════════════════════════
            addCoverSection(document, dateRange);

            // ═══════════════════════════════════════════════════════════════════
            // KPI OVERVIEW SECTION
            // ═══════════════════════════════════════════════════════════════════
            addKpiSection(document, overview);

            // ═══════════════════════════════════════════════════════════════════
            // TOP SELLING BOOKS
            // ═══════════════════════════════════════════════════════════════════
            document.newPage();
            addTopBooksSection(document, topBooks);

            // ═══════════════════════════════════════════════════════════════════
            // CATEGORY REVENUE (if available)
            // ═══════════════════════════════════════════════════════════════════
            if (categoryRevenue != null && !categoryRevenue.isEmpty()) {
                addCategorySection(document, categoryRevenue);
            }

            // ═══════════════════════════════════════════════════════════════════
            // DETAILED REVENUE TABLE
            // ═══════════════════════════════════════════════════════════════════
            document.newPage();
            addRevenueTableSection(document, revenueTable);

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // COVER SECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void addCoverSection(Document document, String dateRange) throws DocumentException {
        // Spacing
        document.add(new Paragraph(" "));

        // Decorative line
        PdfPTable decorLine = new PdfPTable(1);
        decorLine.setWidthPercentage(30);
        decorLine.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBackgroundColor(TEAL_PRIMARY);
        lineCell.setFixedHeight(4);
        lineCell.setBorder(Rectangle.NO_BORDER);
        decorLine.addCell(lineCell);
        document.add(decorLine);

        document.add(new Paragraph(" "));

        // Main title
        Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, TEAL_PRIMARY);
        Paragraph title = new Paragraph("BÁO CÁO DOANH THU", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        // Subtitle
        Font subtitleFont = new Font(Font.HELVETICA, 14, Font.NORMAL, GRAY_500);
        Paragraph subtitle = new Paragraph("BookHaven - Hệ thống quản lý nhà sách", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(25);
        document.add(subtitle);

        // Info box
        PdfPTable infoBox = new PdfPTable(1);
        infoBox.setWidthPercentage(70);
        infoBox.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(TEAL_LIGHT);
        infoCell.setBorderColor(TEAL_PRIMARY);
        infoCell.setBorderWidth(1);
        infoCell.setPadding(15);
        infoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_700);
        Font infoValueFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_DARK);

        Phrase infoPhrase = new Phrase();
        infoPhrase.add(new Chunk("Ngày xuất báo cáo: ", infoFont));
        infoPhrase.add(new Chunk(DATE_FORMAT.format(new Date()), infoValueFont));
        infoPhrase.add(new Chunk("  |  ", infoFont));
        infoPhrase.add(new Chunk("Kỳ báo cáo: ", infoFont));
        infoPhrase.add(new Chunk(formatDateRange(dateRange), infoValueFont));

        infoCell.setPhrase(infoPhrase);
        infoBox.addCell(infoCell);
        document.add(infoBox);

        document.add(new Paragraph(" "));
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // KPI SECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void addKpiSection(Document document, ReportOverviewVm overview) throws DocumentException {
        // Section title
        addSectionTitle(document, "TỔNG QUAN HIỆU SUẤT");

        // KPI Cards Grid (2x2)
        PdfPTable kpiGrid = new PdfPTable(2);
        kpiGrid.setWidthPercentage(100);
        kpiGrid.setSpacingBefore(15);
        kpiGrid.setSpacingAfter(20);
        kpiGrid.setWidths(new float[]{1, 1});

        // Card 1: Total Revenue
        addKpiCard(kpiGrid, "TỔNG DOANH THU",
                   formatCurrency(overview.totalRevenue()) + " đ",
                   formatGrowthText(overview.revenueGrowth()),
                   overview.revenueGrowth() != null && overview.revenueGrowth() >= 0,
                   TEAL_PRIMARY);

        // Card 2: Total Orders
        addKpiCard(kpiGrid, "TỔNG ĐƠN HÀNG",
                   String.valueOf(overview.totalOrders()),
                   formatGrowthText(overview.ordersGrowth()),
                   overview.ordersGrowth() != null && overview.ordersGrowth() >= 0,
                   CORAL);

        // Card 3: Average Order Value
        addKpiCard(kpiGrid, "GIÁ TRỊ ĐƠN TRUNG BÌNH",
                   formatCurrency(overview.avgOrderValue()) + " đ",
                   formatGrowthText(overview.avgValueGrowth()),
                   overview.avgValueGrowth() != null && overview.avgValueGrowth() >= 0,
                   new Color(8, 145, 178));

        // Card 4: New Customers
        addKpiCard(kpiGrid, "KHÁCH HÀNG MỚI",
                   String.valueOf(overview.newCustomers()),
                   formatGrowthText(overview.customersGrowth()),
                   overview.customersGrowth() != null && overview.customersGrowth() >= 0,
                   new Color(139, 92, 246));

        document.add(kpiGrid);

        // Summary stats
        addSummaryBox(document, overview);
    }

    private void addKpiCard(PdfPTable grid, String label, String value, String growth, boolean positive, Color accentColor) {
        PdfPCell card = new PdfPCell();
        card.setPadding(15);
        card.setPaddingTop(12);
        card.setPaddingBottom(12);
        card.setBorderColor(GRAY_200);
        card.setBorderWidth(1);

        // Create inner table for card content
        PdfPTable cardContent = new PdfPTable(1);
        cardContent.setWidthPercentage(100);

        // Accent bar
        PdfPCell accentBar = new PdfPCell();
        accentBar.setBackgroundColor(accentColor);
        accentBar.setFixedHeight(3);
        accentBar.setBorder(Rectangle.NO_BORDER);
        cardContent.addCell(accentBar);

        // Label
        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, GRAY_500);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(10);
        labelCell.setPaddingBottom(5);
        cardContent.addCell(labelCell);

        // Value
        Font valueFont = new Font(Font.HELVETICA, 20, Font.BOLD, GRAY_900);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        cardContent.addCell(valueCell);

        // Growth
        Color growthColor = positive ? SUCCESS : DANGER;
        Font growthFont = new Font(Font.HELVETICA, 9, Font.BOLD, growthColor);
        String arrow = positive ? "↑ " : "↓ ";
        PdfPCell growthCell = new PdfPCell(new Phrase(arrow + growth, growthFont));
        growthCell.setBorder(Rectangle.NO_BORDER);
        cardContent.addCell(growthCell);

        card.addElement(cardContent);
        grid.addCell(card);
    }

    private void addSummaryBox(Document document, ReportOverviewVm overview) throws DocumentException {
        PdfPTable summaryBox = new PdfPTable(3);
        summaryBox.setWidthPercentage(100);
        summaryBox.setSpacingBefore(10);

        double totalRevenue = overview.totalRevenue() != null ? overview.totalRevenue() : 0;
        double estimatedCost = totalRevenue * 0.7;
        double estimatedProfit = totalRevenue * 0.3;

        // Cost
        addSummaryItem(summaryBox, "Chi phí ước tính", formatCurrency(estimatedCost) + " đ", GRAY_700);
        // Profit
        addSummaryItem(summaryBox, "Lợi nhuận ước tính", formatCurrency(estimatedProfit) + " đ", SUCCESS);
        // Margin
        addSummaryItem(summaryBox, "Biên lợi nhuận", "30%", TEAL_PRIMARY);

        document.add(summaryBox);
    }

    private void addSummaryItem(PdfPTable table, String label, String value, Color valueColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBorder(Rectangle.NO_BORDER);
        cell.setPadding(10);
        cell.setBackgroundColor(GRAY_50);

        PdfPTable inner = new PdfPTable(1);
        inner.setWidthPercentage(100);

        Font labelFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_500);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        inner.addCell(labelCell);

        Font valueFont = new Font(Font.HELVETICA, 12, Font.BOLD, valueColor);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        valueCell.setPaddingTop(5);
        inner.addCell(valueCell);

        cell.addElement(inner);
        table.addCell(cell);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // TOP BOOKS SECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void addTopBooksSection(Document document, List<BookSalesVm> topBooks) throws DocumentException {
        addSectionTitle(document, "TOP SÁCH BÁN CHẠY");

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setWidths(new float[]{0.6f, 3f, 2f, 1f, 1.5f});

        // Header
        String[] headers = {"#", "Tên sách", "Tác giả", "Đã bán", "Doanh thu"};
        for (String header : headers) {
            addTableHeader(table, header);
        }

        // Data rows
        for (int i = 0; i < Math.min(topBooks.size(), 10); i++) {
            BookSalesVm book = topBooks.get(i);
            boolean isAlt = i % 2 == 1;
            Color bgColor = isAlt ? GRAY_50 : Color.WHITE;

            // Rank with special styling for top 3
            String rankText;
            Color rankColor;
            if (i == 0) { rankText = "🥇"; rankColor = new Color(251, 191, 36); }
            else if (i == 1) { rankText = "🥈"; rankColor = new Color(163, 163, 163); }
            else if (i == 2) { rankText = "🥉"; rankColor = new Color(217, 119, 6); }
            else { rankText = String.valueOf(i + 1); rankColor = GRAY_700; }

            addTableCell(table, rankText, Element.ALIGN_CENTER, bgColor, rankColor, true);
            addTableCell(table, truncate(book.title(), 35), Element.ALIGN_LEFT, bgColor, GRAY_900, false);
            addTableCell(table, truncate(book.author(), 25), Element.ALIGN_LEFT, bgColor, GRAY_500, false);
            addTableCell(table, String.valueOf(book.soldCount()), Element.ALIGN_CENTER, bgColor, GRAY_700, true);
            addTableCell(table, formatCurrency(book.revenue()) + "đ", Element.ALIGN_RIGHT, bgColor, TEAL_PRIMARY, true);
        }

        document.add(table);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // CATEGORY SECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void addCategorySection(Document document, List<CategoryRevenueVm> categoryRevenue) throws DocumentException {
        document.add(new Paragraph(" "));
        addSectionTitle(document, "DOANH THU THEO DANH MỤC");

        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1f});

        // Header
        String[] headers = {"#", "Danh mục", "Doanh thu", "Tỷ lệ"};
        for (String header : headers) {
            addTableHeader(table, header);
        }

        // Data rows
        Color[] categoryColors = {TEAL_PRIMARY, CORAL, new Color(139, 92, 246), new Color(8, 145, 178), new Color(236, 72, 153), GRAY_500};

        for (int i = 0; i < categoryRevenue.size(); i++) {
            CategoryRevenueVm category = categoryRevenue.get(i);
            boolean isAlt = i % 2 == 1;
            Color bgColor = isAlt ? GRAY_50 : Color.WHITE;
            Color accentColor = categoryColors[i % categoryColors.length];

            addTableCell(table, String.valueOf(i + 1), Element.ALIGN_CENTER, bgColor, GRAY_700, true);
            addTableCellWithBar(table, category.categoryName(), bgColor, accentColor);
            addTableCell(table, formatCurrency(category.revenue()) + "đ", Element.ALIGN_RIGHT, bgColor, TEAL_PRIMARY, true);
            addTableCell(table, category.percentage() + "%", Element.ALIGN_CENTER, bgColor, GRAY_700, true);
        }

        document.add(table);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // REVENUE TABLE SECTION
    // ═══════════════════════════════════════════════════════════════════════════

    private void addRevenueTableSection(Document document, List<RevenueTableRowVm> revenueTable) throws DocumentException {
        addSectionTitle(document, "CHI TIẾT DOANH THU");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setWidths(new float[]{1.5f, 1f, 1.3f, 1.3f, 1.3f, 1f});

        // Header
        String[] headers = {"Thời gian", "Đơn hàng", "Doanh thu", "Chi phí", "Lợi nhuận", "Tăng trưởng"};
        for (String header : headers) {
            addTableHeader(table, header);
        }

        // Data rows
        double totalRevenue = 0;
        double totalCost = 0;
        double totalProfit = 0;
        long totalOrders = 0;

        for (int i = 0; i < revenueTable.size(); i++) {
            RevenueTableRowVm row = revenueTable.get(i);
            boolean isAlt = i % 2 == 1;
            Color bgColor = isAlt ? GRAY_50 : Color.WHITE;

            addTableCell(table, row.periodLabel(), Element.ALIGN_LEFT, bgColor, GRAY_900, true);
            addTableCell(table, String.valueOf(row.orderCount()), Element.ALIGN_CENTER, bgColor, GRAY_700, false);
            addTableCell(table, formatCurrency(row.revenue()) + "đ", Element.ALIGN_RIGHT, bgColor, TEAL_PRIMARY, true);
            addTableCell(table, formatCurrency(row.cost()) + "đ", Element.ALIGN_RIGHT, bgColor, GRAY_500, false);
            addTableCell(table, formatCurrency(row.profit()) + "đ", Element.ALIGN_RIGHT, bgColor, SUCCESS, true);

            // Growth with color
            boolean positive = row.growthPercent() != null && row.growthPercent() >= 0;
            Color growthColor = positive ? SUCCESS : DANGER;
            String growthText = formatGrowth(row.growthPercent());
            addTableCell(table, growthText, Element.ALIGN_CENTER, bgColor, growthColor, true);

            totalRevenue += row.revenue() != null ? row.revenue() : 0;
            totalCost += row.cost() != null ? row.cost() : 0;
            totalProfit += row.profit() != null ? row.profit() : 0;
            totalOrders += row.orderCount() != null ? row.orderCount() : 0;
        }

        // Summary row
        addSummaryRow(table, "TỔNG CỘNG", totalOrders, totalRevenue, totalCost, totalProfit);

        document.add(table);
    }

    private void addSummaryRow(PdfPTable table, String label, long orders, double revenue, double cost, double profit) {
        Font summaryFont = new Font(Font.HELVETICA, 10, Font.BOLD, GRAY_900);
        Font summaryValueFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_PRIMARY);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, summaryFont));
        labelCell.setBackgroundColor(TEAL_LIGHT);
        labelCell.setBorderColor(GRAY_200);
        labelCell.setPadding(10);
        table.addCell(labelCell);

        PdfPCell ordersCell = new PdfPCell(new Phrase(String.valueOf(orders), summaryFont));
        ordersCell.setBackgroundColor(TEAL_LIGHT);
        ordersCell.setBorderColor(GRAY_200);
        ordersCell.setPadding(10);
        ordersCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(ordersCell);

        PdfPCell revenueCell = new PdfPCell(new Phrase(formatCurrency(revenue) + "đ", summaryValueFont));
        revenueCell.setBackgroundColor(TEAL_LIGHT);
        revenueCell.setBorderColor(GRAY_200);
        revenueCell.setPadding(10);
        revenueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(revenueCell);

        PdfPCell costCell = new PdfPCell(new Phrase(formatCurrency(cost) + "đ", summaryFont));
        costCell.setBackgroundColor(TEAL_LIGHT);
        costCell.setBorderColor(GRAY_200);
        costCell.setPadding(10);
        costCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(costCell);

        Font profitFont = new Font(Font.HELVETICA, 10, Font.BOLD, SUCCESS);
        PdfPCell profitCell = new PdfPCell(new Phrase(formatCurrency(profit) + "đ", profitFont));
        profitCell.setBackgroundColor(TEAL_LIGHT);
        profitCell.setBorderColor(GRAY_200);
        profitCell.setPadding(10);
        profitCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(profitCell);

        PdfPCell emptyCell = new PdfPCell(new Phrase("-", summaryFont));
        emptyCell.setBackgroundColor(TEAL_LIGHT);
        emptyCell.setBorderColor(GRAY_200);
        emptyCell.setPadding(10);
        emptyCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(emptyCell);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void addSectionTitle(Document document, String title) throws DocumentException {
        // Decorative line
        PdfPTable decorLine = new PdfPTable(new float[]{0.05f, 0.95f});
        decorLine.setWidthPercentage(100);
        decorLine.setSpacingBefore(10);

        PdfPCell accentCell = new PdfPCell();
        accentCell.setBackgroundColor(TEAL_PRIMARY);
        accentCell.setBorder(Rectangle.NO_BORDER);
        accentCell.setFixedHeight(24);
        decorLine.addCell(accentCell);

        Font titleFont = new Font(Font.HELVETICA, 14, Font.BOLD, TEAL_DARK);
        PdfPCell titleCell = new PdfPCell(new Phrase("  " + title, titleFont));
        titleCell.setBorder(Rectangle.NO_BORDER);
        titleCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        titleCell.setBackgroundColor(GRAY_50);
        titleCell.setPaddingLeft(10);
        decorLine.addCell(titleCell);

        document.add(decorLine);
    }

    private void addTableHeader(PdfPTable table, String text) {
        Font headerFont = new Font(Font.HELVETICA, 9, Font.BOLD, Color.WHITE);
        PdfPCell cell = new PdfPCell(new Phrase(text, headerFont));
        cell.setBackgroundColor(TEAL_PRIMARY);
        cell.setPadding(10);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(TEAL_DARK);
        table.addCell(cell);
    }

    private void addTableCell(PdfPTable table, String text, int align, Color bgColor, Color textColor, boolean bold) {
        Font font = new Font(Font.HELVETICA, 9, bold ? Font.BOLD : Font.NORMAL, textColor);
        PdfPCell cell = new PdfPCell(new Phrase(text, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(8);
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBorderColor(GRAY_200);
        table.addCell(cell);
    }

    private void addTableCellWithBar(PdfPTable table, String text, Color bgColor, Color barColor) {
        PdfPCell cell = new PdfPCell();
        cell.setBackgroundColor(bgColor);
        cell.setPadding(0);
        cell.setBorderColor(GRAY_200);

        PdfPTable inner = new PdfPTable(new float[]{0.02f, 0.98f});
        inner.setWidthPercentage(100);

        PdfPCell barCell = new PdfPCell();
        barCell.setBackgroundColor(barColor);
        barCell.setBorder(Rectangle.NO_BORDER);
        inner.addCell(barCell);

        Font font = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_900);
        PdfPCell textCell = new PdfPCell(new Phrase(text, font));
        textCell.setBorder(Rectangle.NO_BORDER);
        textCell.setPadding(8);
        textCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        inner.addCell(textCell);

        cell.addElement(inner);
        table.addCell(cell);
    }

    private String formatCurrency(Double value) {
        if (value == null) return "0";
        return CURRENCY_FORMAT.format(value);
    }

    private String formatGrowth(Double value) {
        if (value == null) return "0%";
        return (value >= 0 ? "+" : "") + String.format("%.1f", value) + "%";
    }

    private String formatGrowthText(Double value) {
        if (value == null) return "Không đổi";
        return Math.abs(value) + "% so với kỳ trước";
    }

    private String formatDateRange(String range) {
        switch (range.toLowerCase()) {
            case "today": return "Hôm nay";
            case "week": return "7 ngày qua";
            case "month": return "30 ngày qua";
            case "quarter": return "Quý này";
            case "year": return "Năm nay";
            default: return range;
        }
    }

    private String truncate(String text, int maxLength) {
        if (text == null) return "";
        if (text.length() <= maxLength) return text;
        return text.substring(0, maxLength - 3) + "...";
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HEADER/FOOTER PAGE EVENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static class HeaderFooterPageEvent extends PdfPageEventHelper {
        private final String dateRange;

        public HeaderFooterPageEvent(String dateRange) {
            this.dateRange = dateRange;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Header line
            cb.setColorStroke(TEAL_PRIMARY);
            cb.setLineWidth(2);
            cb.moveTo(40, document.top() + 20);
            cb.lineTo(document.right() - 40 + document.leftMargin(), document.top() + 20);
            cb.stroke();

            // Footer
            cb.setColorStroke(GRAY_200);
            cb.setLineWidth(0.5f);
            cb.moveTo(40, document.bottom() - 20);
            cb.lineTo(document.right() - 40 + document.leftMargin(), document.bottom() - 20);
            cb.stroke();

            // Footer text
            Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_500);

            // Left: Company
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("BookHaven - Báo cáo doanh thu", footerFont),
                    40, document.bottom() - 35, 0);

            // Center: Page number
            Font pageFont = new Font(Font.HELVETICA, 8, Font.BOLD, TEAL_PRIMARY);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Trang " + writer.getPageNumber(), pageFont),
                    (document.right() + document.left()) / 2, document.bottom() - 35, 0);

            // Right: Date
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()), footerFont),
                    document.right() - 40 + document.leftMargin(), document.bottom() - 35, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INVENTORY REPORT EXPORT
    // ═══════════════════════════════════════════════════════════════════════════

    /**
     * Export inventory report to PDF.
     * Shows stock status, low stock warnings, and inventory summary.
     */
    public byte[] exportInventoryReport(
            List<InventoryItemVm> items,
            InventorySummaryVm summary
    ) throws DocumentException, IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        Document document = new Document(PageSize.A4, 40, 40, 60, 50);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, outputStream);

            // Add page event for header/footer
            writer.setPageEvent(new InventoryHeaderFooterPageEvent());

            document.open();

            // Cover Section
            addInventoryCoverSection(document);

            // Summary Cards
            addInventorySummarySection(document, summary);

            // Alert Section (if there are low stock items)
            long lowStockCount = items.stream().filter(i -> i.quantity() > 0 && i.quantity() <= 5).count();
            long outOfStockCount = items.stream().filter(i -> i.quantity() == 0).count();
            if (lowStockCount > 0 || outOfStockCount > 0) {
                addInventoryAlertSection(document, lowStockCount, outOfStockCount);
            }

            // Inventory Table
            document.newPage();
            addInventoryTableSection(document, items);

            // Low Stock Items (separate section)
            List<InventoryItemVm> lowStockItems = items.stream()
                    .filter(i -> i.quantity() != null && i.quantity() <= 5)
                    .sorted((a, b) -> Integer.compare(a.quantity(), b.quantity()))
                    .toList();

            if (!lowStockItems.isEmpty()) {
                document.newPage();
                addLowStockSection(document, lowStockItems);
            }

        } finally {
            document.close();
        }

        return outputStream.toByteArray();
    }

    private void addInventoryCoverSection(Document document) throws DocumentException {
        // Spacing
        document.add(new Paragraph(" "));

        // Decorative line
        PdfPTable decorLine = new PdfPTable(1);
        decorLine.setWidthPercentage(30);
        decorLine.setHorizontalAlignment(Element.ALIGN_CENTER);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBackgroundColor(TEAL_PRIMARY);
        lineCell.setFixedHeight(4);
        lineCell.setBorder(Rectangle.NO_BORDER);
        decorLine.addCell(lineCell);
        document.add(decorLine);

        document.add(new Paragraph(" "));

        // Main title with icon
        Font titleFont = new Font(Font.HELVETICA, 28, Font.BOLD, TEAL_PRIMARY);
        Paragraph title = new Paragraph("BÁO CÁO TỒN KHO", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(8);
        document.add(title);

        // Subtitle
        Font subtitleFont = new Font(Font.HELVETICA, 14, Font.NORMAL, GRAY_500);
        Paragraph subtitle = new Paragraph("BookHaven - Hệ thống quản lý nhà sách", subtitleFont);
        subtitle.setAlignment(Element.ALIGN_CENTER);
        subtitle.setSpacingAfter(25);
        document.add(subtitle);

        // Info box
        PdfPTable infoBox = new PdfPTable(1);
        infoBox.setWidthPercentage(70);
        infoBox.setHorizontalAlignment(Element.ALIGN_CENTER);

        PdfPCell infoCell = new PdfPCell();
        infoCell.setBackgroundColor(TEAL_LIGHT);
        infoCell.setBorderColor(TEAL_PRIMARY);
        infoCell.setBorderWidth(1);
        infoCell.setPadding(15);
        infoCell.setHorizontalAlignment(Element.ALIGN_CENTER);

        Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, GRAY_700);
        Font infoValueFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_DARK);

        Phrase infoPhrase = new Phrase();
        infoPhrase.add(new Chunk("Ngày xuất báo cáo: ", infoFont));
        infoPhrase.add(new Chunk(DATE_FORMAT.format(new Date()), infoValueFont));

        infoCell.setPhrase(infoPhrase);
        infoBox.addCell(infoCell);
        document.add(infoBox);

        document.add(new Paragraph(" "));
    }

    private void addInventorySummarySection(Document document, InventorySummaryVm summary) throws DocumentException {
        addSectionTitle(document, "TỔNG QUAN TỒN KHO");

        // KPI Cards Grid (2x2)
        PdfPTable kpiGrid = new PdfPTable(2);
        kpiGrid.setWidthPercentage(100);
        kpiGrid.setSpacingBefore(15);
        kpiGrid.setSpacingAfter(20);
        kpiGrid.setWidths(new float[]{1, 1});

        // Card 1: Total Products
        addInventoryKpiCard(kpiGrid, "TỔNG SỐ SÁCH",
                String.valueOf(summary.totalProducts()),
                "Sản phẩm trong kho",
                TEAL_PRIMARY);

        // Card 2: Total Stock
        addInventoryKpiCard(kpiGrid, "TỔNG SỐ LƯỢNG",
                formatNumber(summary.totalStock()),
                "Đơn vị trong kho",
                CORAL);

        // Card 3: In Stock
        addInventoryKpiCard(kpiGrid, "CÒN HÀNG",
                String.valueOf(summary.inStockCount()),
                summary.inStockPercent() + "% sản phẩm",
                SUCCESS);

        // Card 4: Low Stock + Out of Stock
        addInventoryKpiCard(kpiGrid, "CẦN NHẬP THÊM",
                String.valueOf(summary.lowStockCount() + summary.outOfStockCount()),
                summary.lowStockCount() + " sắp hết, " + summary.outOfStockCount() + " hết hàng",
                DANGER);

        document.add(kpiGrid);

        // Stock Value Summary
        addStockValueBox(document, summary);
    }

    private void addInventoryKpiCard(PdfPTable grid, String label, String value, String subtext, Color accentColor) {
        PdfPCell card = new PdfPCell();
        card.setPadding(15);
        card.setPaddingTop(12);
        card.setPaddingBottom(12);
        card.setBorderColor(GRAY_200);
        card.setBorderWidth(1);

        PdfPTable cardContent = new PdfPTable(1);
        cardContent.setWidthPercentage(100);

        // Accent bar
        PdfPCell accentBar = new PdfPCell();
        accentBar.setBackgroundColor(accentColor);
        accentBar.setFixedHeight(3);
        accentBar.setBorder(Rectangle.NO_BORDER);
        cardContent.addCell(accentBar);

        // Label
        Font labelFont = new Font(Font.HELVETICA, 8, Font.BOLD, GRAY_500);
        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorder(Rectangle.NO_BORDER);
        labelCell.setPaddingTop(10);
        labelCell.setPaddingBottom(5);
        cardContent.addCell(labelCell);

        // Value
        Font valueFont = new Font(Font.HELVETICA, 24, Font.BOLD, GRAY_900);
        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setPaddingBottom(5);
        cardContent.addCell(valueCell);

        // Subtext
        Font subtextFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_500);
        PdfPCell subtextCell = new PdfPCell(new Phrase(subtext, subtextFont));
        subtextCell.setBorder(Rectangle.NO_BORDER);
        cardContent.addCell(subtextCell);

        card.addElement(cardContent);
        grid.addCell(card);
    }

    private void addStockValueBox(Document document, InventorySummaryVm summary) throws DocumentException {
        PdfPTable summaryBox = new PdfPTable(3);
        summaryBox.setWidthPercentage(100);
        summaryBox.setSpacingBefore(10);

        // Total Value
        addSummaryItem(summaryBox, "Giá trị tồn kho", formatCurrency(summary.totalValue()) + " đ", TEAL_PRIMARY);
        // Average Price
        addSummaryItem(summaryBox, "Giá trung bình/sản phẩm", formatCurrency(summary.avgPrice()) + " đ", GRAY_700);
        // Stock Health
        String healthStatus = summary.inStockPercent() >= 80 ? "Tốt" :
                              summary.inStockPercent() >= 50 ? "Trung bình" : "Cần cải thiện";
        Color healthColor = summary.inStockPercent() >= 80 ? SUCCESS :
                           summary.inStockPercent() >= 50 ? CORAL : DANGER;
        addSummaryItem(summaryBox, "Tình trạng kho", healthStatus, healthColor);

        document.add(summaryBox);
    }

    private void addInventoryAlertSection(Document document, long lowStockCount, long outOfStockCount) throws DocumentException {
        document.add(new Paragraph(" "));

        // Alert box
        PdfPTable alertBox = new PdfPTable(1);
        alertBox.setWidthPercentage(100);

        PdfPCell alertCell = new PdfPCell();
        alertCell.setBackgroundColor(new Color(254, 243, 199)); // Amber 100
        alertCell.setBorderColor(CORAL);
        alertCell.setBorderWidth(2);
        alertCell.setPadding(15);

        Font alertTitleFont = new Font(Font.HELVETICA, 11, Font.BOLD, new Color(146, 64, 14));
        Font alertTextFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(161, 98, 7));

        Paragraph alertContent = new Paragraph();
        alertContent.add(new Chunk("⚠ CẢNH BÁO TỒN KHO\n", alertTitleFont));
        alertContent.add(new Chunk("Có " + lowStockCount + " sản phẩm sắp hết hàng và " +
                outOfStockCount + " sản phẩm đã hết hàng. Vui lòng kiểm tra và nhập thêm hàng để đảm bảo cung ứng.", alertTextFont));

        alertCell.addElement(alertContent);
        alertBox.addCell(alertCell);
        document.add(alertBox);
    }

    private void addInventoryTableSection(Document document, List<InventoryItemVm> items) throws DocumentException {
        addSectionTitle(document, "CHI TIẾT TỒN KHO");

        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        table.setSpacingBefore(15);
        table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1.2f, 1f, 1f});

        // Header
        String[] headers = {"#", "Tên sách", "Danh mục", "Giá bán", "Số lượng", "Trạng thái"};
        for (String header : headers) {
            addTableHeader(table, header);
        }

        // Data rows
        for (int i = 0; i < items.size(); i++) {
            InventoryItemVm item = items.get(i);
            boolean isAlt = i % 2 == 1;
            Color bgColor = isAlt ? GRAY_50 : Color.WHITE;

            // Determine status
            String status;
            Color statusColor;
            if (item.quantity() == 0) {
                status = "Hết hàng";
                statusColor = DANGER;
                bgColor = new Color(254, 242, 242); // Red 50
            } else if (item.quantity() <= 5) {
                status = "Sắp hết";
                statusColor = CORAL;
                bgColor = new Color(255, 251, 235); // Amber 50
            } else {
                status = "Còn hàng";
                statusColor = SUCCESS;
            }

            addTableCell(table, String.valueOf(i + 1), Element.ALIGN_CENTER, bgColor, GRAY_700, true);
            addTableCell(table, truncate(item.title(), 30), Element.ALIGN_LEFT, bgColor, GRAY_900, false);
            addTableCell(table, item.categoryName() != null ? item.categoryName() : "Chưa phân loại",
                        Element.ALIGN_LEFT, bgColor, GRAY_500, false);
            addTableCell(table, formatCurrency(item.price()) + "đ", Element.ALIGN_RIGHT, bgColor, TEAL_PRIMARY, true);
            addTableCell(table, String.valueOf(item.quantity()), Element.ALIGN_CENTER, bgColor, GRAY_700, true);
            addTableCell(table, status, Element.ALIGN_CENTER, bgColor, statusColor, true);
        }

        document.add(table);
    }

    private void addLowStockSection(Document document, List<InventoryItemVm> lowStockItems) throws DocumentException {
        addSectionTitle(document, "SẢN PHẨM CẦN NHẬP THÊM");

        // Description
        Font descFont = new Font(Font.HELVETICA, 10, Font.ITALIC, GRAY_500);
        Paragraph desc = new Paragraph("Danh sách các sản phẩm có số lượng tồn kho thấp, cần được nhập thêm hàng.", descFont);
        desc.setSpacingAfter(15);
        document.add(desc);

        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setWidths(new float[]{0.5f, 2.5f, 1.5f, 1f, 1.5f});

        // Header
        String[] headers = {"#", "Tên sách", "Danh mục", "Số lượng", "Mức độ"};
        for (String header : headers) {
            addTableHeader(table, header);
        }

        // Data rows
        for (int i = 0; i < lowStockItems.size(); i++) {
            InventoryItemVm item = lowStockItems.get(i);
            boolean isAlt = i % 2 == 1;
            Color bgColor = isAlt ? GRAY_50 : Color.WHITE;

            // Priority level
            String priority;
            Color priorityColor;
            if (item.quantity() == 0) {
                priority = "🔴 Khẩn cấp";
                priorityColor = DANGER;
                bgColor = new Color(254, 242, 242);
            } else if (item.quantity() <= 2) {
                priority = "🟠 Cao";
                priorityColor = CORAL;
                bgColor = new Color(255, 251, 235);
            } else {
                priority = "🟡 Trung bình";
                priorityColor = new Color(202, 138, 4);
            }

            addTableCell(table, String.valueOf(i + 1), Element.ALIGN_CENTER, bgColor, GRAY_700, true);
            addTableCell(table, truncate(item.title(), 30), Element.ALIGN_LEFT, bgColor, GRAY_900, false);
            addTableCell(table, item.categoryName() != null ? item.categoryName() : "Chưa phân loại",
                        Element.ALIGN_LEFT, bgColor, GRAY_500, false);
            addTableCell(table, String.valueOf(item.quantity()), Element.ALIGN_CENTER, bgColor, GRAY_900, true);
            addTableCell(table, priority, Element.ALIGN_CENTER, bgColor, priorityColor, true);
        }

        document.add(table);

        // Recommendation
        document.add(new Paragraph(" "));
        PdfPTable recBox = new PdfPTable(1);
        recBox.setWidthPercentage(100);

        PdfPCell recCell = new PdfPCell();
        recCell.setBackgroundColor(TEAL_LIGHT);
        recCell.setBorderColor(TEAL_PRIMARY);
        recCell.setBorderWidth(1);
        recCell.setPadding(15);

        Font recTitleFont = new Font(Font.HELVETICA, 10, Font.BOLD, TEAL_DARK);
        Font recTextFont = new Font(Font.HELVETICA, 9, Font.NORMAL, GRAY_700);

        Paragraph recContent = new Paragraph();
        recContent.add(new Chunk("💡 KHUYẾN NGHỊ\n", recTitleFont));
        recContent.add(new Chunk("• Ưu tiên nhập hàng cho các sản phẩm có mức độ 'Khẩn cấp' trước\n", recTextFont));
        recContent.add(new Chunk("• Xem xét đặt hàng với số lượng lớn hơn để tránh tình trạng hết hàng\n", recTextFont));
        recContent.add(new Chunk("• Theo dõi xu hướng bán hàng để dự đoán nhu cầu tồn kho", recTextFont));

        recCell.addElement(recContent);
        recBox.addCell(recCell);
        document.add(recBox);
    }

    private String formatNumber(long value) {
        return CURRENCY_FORMAT.format(value);
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INVENTORY HEADER/FOOTER PAGE EVENT
    // ═══════════════════════════════════════════════════════════════════════════

    private static class InventoryHeaderFooterPageEvent extends PdfPageEventHelper {

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Header line
            cb.setColorStroke(TEAL_PRIMARY);
            cb.setLineWidth(2);
            cb.moveTo(40, document.top() + 20);
            cb.lineTo(document.right() - 40 + document.leftMargin(), document.top() + 20);
            cb.stroke();

            // Footer
            cb.setColorStroke(GRAY_200);
            cb.setLineWidth(0.5f);
            cb.moveTo(40, document.bottom() - 20);
            cb.lineTo(document.right() - 40 + document.leftMargin(), document.bottom() - 20);
            cb.stroke();

            // Footer text
            Font footerFont = new Font(Font.HELVETICA, 8, Font.NORMAL, GRAY_500);

            // Left: Company
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    new Phrase("BookHaven - Báo cáo tồn kho", footerFont),
                    40, document.bottom() - 35, 0);

            // Center: Page number
            Font pageFont = new Font(Font.HELVETICA, 8, Font.BOLD, TEAL_PRIMARY);
            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    new Phrase("Trang " + writer.getPageNumber(), pageFont),
                    (document.right() + document.left()) / 2, document.bottom() - 35, 0);

            // Right: Date
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase(new SimpleDateFormat("dd/MM/yyyy").format(new java.util.Date()), footerFont),
                    document.right() - 40 + document.leftMargin(), document.bottom() - 35, 0);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // INVENTORY VIEW MODELS (Inner Records)
    // ═══════════════════════════════════════════════════════════════════════════

    public record InventoryItemVm(
            String id,
            String title,
            String author,
            String categoryName,
            Double price,
            Integer quantity
    ) {}

    public record InventorySummaryVm(
            long totalProducts,
            long totalStock,
            long inStockCount,
            long lowStockCount,
            long outOfStockCount,
            int inStockPercent,
            Double totalValue,
            Double avgPrice
    ) {}
}
