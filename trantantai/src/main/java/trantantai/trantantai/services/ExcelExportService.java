package trantantai.trantantai.services;

import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.stereotype.Service;
import trantantai.trantantai.viewmodels.*;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Service for exporting reports to Excel format.
 * Professional styling with multiple sheets and charts.
 */
@Service
public class ExcelExportService {

    private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("#,###");
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    // Brand colors (used in various style methods via IndexedColors directly)

    /**
     * Export full report to Excel with multiple sheets.
     */
    public byte[] exportReport(
            ReportOverviewVm overview,
            List<BookSalesVm> topBooks,
            List<RevenueTableRowVm> revenueTable,
            String dateRange
    ) throws IOException {
        return exportReport(overview, topBooks, revenueTable, null, dateRange);
    }

    /**
     * Export full report to Excel with multiple sheets including category data.
     */
    public byte[] exportReport(
            ReportOverviewVm overview,
            List<BookSalesVm> topBooks,
            List<RevenueTableRowVm> revenueTable,
            List<CategoryRevenueVm> categoryRevenue,
            String dateRange
    ) throws IOException {
        try (XSSFWorkbook workbook = new XSSFWorkbook()) {
            // Create custom colors
            XSSFColor tealColor = new XSSFColor(new byte[]{(byte)13, (byte)148, (byte)136}, null);
            XSSFColor lightTeal = new XSSFColor(new byte[]{(byte)240, (byte)253, (byte)250}, null);
            XSSFColor lightGray = new XSSFColor(new byte[]{(byte)250, (byte)250, (byte)249}, null);

            // Create styles
            CellStyle titleStyle = createTitleStyle(workbook, tealColor);
            CellStyle subtitleStyle = createSubtitleStyle(workbook);
            CellStyle headerStyle = createHeaderStyle(workbook, tealColor);
            CellStyle dataStyle = createDataStyle(workbook);
            CellStyle dataStyleAlt = createDataStyleAlt(workbook, lightGray);
            CellStyle currencyStyle = createCurrencyStyle(workbook);
            CellStyle currencyStyleAlt = createCurrencyStyleAlt(workbook, lightGray);
            CellStyle positiveStyle = createGrowthStyle(workbook, true);
            CellStyle negativeStyle = createGrowthStyle(workbook, false);
            CellStyle summaryStyle = createSummaryStyle(workbook, lightTeal);
            CellStyle kpiLabelStyle = createKpiLabelStyle(workbook);
            CellStyle kpiValueStyle = createKpiValueStyle(workbook, tealColor);

            // Create sheets
            createOverviewSheet(workbook, overview, dateRange, titleStyle, subtitleStyle, kpiLabelStyle, kpiValueStyle, positiveStyle, negativeStyle);
            createRevenueTableSheet(workbook, revenueTable, titleStyle, headerStyle, dataStyle, dataStyleAlt, currencyStyle, currencyStyleAlt, positiveStyle, negativeStyle, summaryStyle);
            createTopBooksSheet(workbook, topBooks, titleStyle, headerStyle, dataStyle, dataStyleAlt, currencyStyle, currencyStyleAlt);
            if (categoryRevenue != null && !categoryRevenue.isEmpty()) {
                createCategorySheet(workbook, categoryRevenue, titleStyle, headerStyle, dataStyle, dataStyleAlt, currencyStyle, currencyStyleAlt);
            }

            // Write to byte array
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            workbook.write(outputStream);
            return outputStream.toByteArray();
        }
    }

    private void createOverviewSheet(XSSFWorkbook workbook, ReportOverviewVm overview, String dateRange,
                                     CellStyle titleStyle, CellStyle subtitleStyle,
                                     CellStyle kpiLabelStyle, CellStyle kpiValueStyle,
                                     CellStyle positiveStyle, CellStyle negativeStyle) {
        XSSFSheet sheet = workbook.createSheet("📊 Tổng Quan");

        // Set column widths
        sheet.setColumnWidth(0, 3000);
        sheet.setColumnWidth(1, 8000);
        sheet.setColumnWidth(2, 6000);
        sheet.setColumnWidth(3, 6000);
        sheet.setColumnWidth(4, 6000);

        int rowNum = 0;

        // ═══════════════════════════════════════════════════════════════════
        // HEADER SECTION
        // ═══════════════════════════════════════════════════════════════════

        // Empty row for spacing
        sheet.createRow(rowNum++);

        // Logo/Title Row
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(36);
        Cell titleCell = titleRow.createCell(1);
        titleCell.setCellValue("📚 BOOKHAVEN - BÁO CÁO DOANH THU");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 4));

        // Subtitle row
        Row subtitleRow = sheet.createRow(rowNum++);
        Cell subtitleCell = subtitleRow.createCell(1);
        subtitleCell.setCellValue("Báo cáo tổng hợp kinh doanh");
        subtitleCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 4));

        // Date info row
        Row dateRow = sheet.createRow(rowNum++);
        Cell dateCell = dateRow.createCell(1);
        dateCell.setCellValue("📅 Ngày xuất: " + DATE_FORMAT.format(new Date()) + "  |  🗓 Kỳ báo cáo: " + formatDateRange(dateRange));
        dateCell.setCellStyle(subtitleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 4));

        // Spacing
        rowNum += 2;

        // ═══════════════════════════════════════════════════════════════════
        // KPI CARDS SECTION
        // ═══════════════════════════════════════════════════════════════════

        Row sectionRow = sheet.createRow(rowNum++);
        sectionRow.setHeightInPoints(24);
        Cell sectionCell = sectionRow.createCell(1);
        sectionCell.setCellValue("▎ CHỈ SỐ HIỆU SUẤT CHÍNH (KPIs)");
        sectionCell.setCellStyle(createSectionHeaderStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 4));

        rowNum++; // Spacing

        // KPI Grid
        // Row 1: Revenue & Orders
        Row kpiRow1 = sheet.createRow(rowNum++);
        kpiRow1.setHeightInPoints(20);
        createKpiCell(kpiRow1, 1, "💰 Tổng Doanh Thu", kpiLabelStyle);
        createKpiCell(kpiRow1, 3, "🛒 Tổng Đơn Hàng", kpiLabelStyle);

        Row kpiValueRow1 = sheet.createRow(rowNum++);
        kpiValueRow1.setHeightInPoints(28);
        Cell revenueCell = kpiValueRow1.createCell(1);
        revenueCell.setCellValue(formatCurrency(overview.totalRevenue()) + " đ");
        revenueCell.setCellStyle(kpiValueStyle);
        Cell ordersCell = kpiValueRow1.createCell(3);
        ordersCell.setCellValue(String.valueOf(overview.totalOrders()));
        ordersCell.setCellStyle(kpiValueStyle);

        Row kpiGrowthRow1 = sheet.createRow(rowNum++);
        Cell revenueGrowthCell = kpiGrowthRow1.createCell(1);
        revenueGrowthCell.setCellValue(formatGrowthText(overview.revenueGrowth()));
        revenueGrowthCell.setCellStyle(overview.revenueGrowth() >= 0 ? positiveStyle : negativeStyle);
        Cell ordersGrowthCell = kpiGrowthRow1.createCell(3);
        ordersGrowthCell.setCellValue(formatGrowthText(overview.ordersGrowth()));
        ordersGrowthCell.setCellStyle(overview.ordersGrowth() >= 0 ? positiveStyle : negativeStyle);

        rowNum++; // Spacing

        // Row 2: Avg Value & New Customers
        Row kpiRow2 = sheet.createRow(rowNum++);
        kpiRow2.setHeightInPoints(20);
        createKpiCell(kpiRow2, 1, "📈 Giá Trị Đơn TB", kpiLabelStyle);
        createKpiCell(kpiRow2, 3, "👥 Khách Hàng Mới", kpiLabelStyle);

        Row kpiValueRow2 = sheet.createRow(rowNum++);
        kpiValueRow2.setHeightInPoints(28);
        Cell avgCell = kpiValueRow2.createCell(1);
        avgCell.setCellValue(formatCurrency(overview.avgOrderValue()) + " đ");
        avgCell.setCellStyle(kpiValueStyle);
        Cell customersCell = kpiValueRow2.createCell(3);
        customersCell.setCellValue(String.valueOf(overview.newCustomers()));
        customersCell.setCellStyle(kpiValueStyle);

        Row kpiGrowthRow2 = sheet.createRow(rowNum++);
        Cell avgGrowthCell = kpiGrowthRow2.createCell(1);
        avgGrowthCell.setCellValue(formatGrowthText(overview.avgValueGrowth()));
        avgGrowthCell.setCellStyle(overview.avgValueGrowth() >= 0 ? positiveStyle : negativeStyle);
        Cell customersGrowthCell = kpiGrowthRow2.createCell(3);
        customersGrowthCell.setCellValue(formatGrowthText(overview.customersGrowth()));
        customersGrowthCell.setCellStyle(overview.customersGrowth() >= 0 ? positiveStyle : negativeStyle);

        // ═══════════════════════════════════════════════════════════════════
        // SUMMARY SECTION
        // ═══════════════════════════════════════════════════════════════════
        rowNum += 2;

        Row summarySection = sheet.createRow(rowNum++);
        summarySection.setHeightInPoints(24);
        Cell summarySectionCell = summarySection.createCell(1);
        summarySectionCell.setCellValue("▎ TÓM TẮT");
        summarySectionCell.setCellStyle(createSectionHeaderStyle(workbook));
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 1, 4));

        rowNum++;

        // Calculate totals
        double estimatedCost = overview.totalRevenue() != null ? overview.totalRevenue() * 0.7 : 0;
        double estimatedProfit = overview.totalRevenue() != null ? overview.totalRevenue() * 0.3 : 0;

        createSummaryRow(sheet, rowNum++, "Chi phí ước tính (70%)", formatCurrency(estimatedCost) + " đ", subtitleStyle);
        createSummaryRow(sheet, rowNum++, "Lợi nhuận ước tính (30%)", formatCurrency(estimatedProfit) + " đ", subtitleStyle);
        createSummaryRow(sheet, rowNum++, "Biên lợi nhuận", "30%", subtitleStyle);

        // Footer
        rowNum += 3;
        Row footerRow = sheet.createRow(rowNum);
        Cell footerCell = footerRow.createCell(1);
        footerCell.setCellValue("© " + java.util.Calendar.getInstance().get(java.util.Calendar.YEAR) + " BookHaven - Hệ thống quản lý nhà sách");
        footerCell.setCellStyle(subtitleStyle);
    }

    private void createRevenueTableSheet(XSSFWorkbook workbook, List<RevenueTableRowVm> revenueTable,
                                          CellStyle titleStyle, CellStyle headerStyle,
                                          CellStyle dataStyle, CellStyle dataStyleAlt,
                                          CellStyle currencyStyle, CellStyle currencyStyleAlt,
                                          CellStyle positiveStyle, CellStyle negativeStyle,
                                          CellStyle summaryStyle) {
        XSSFSheet sheet = workbook.createSheet("📈 Doanh Thu Chi Tiết");

        // Column widths
        sheet.setColumnWidth(0, 5500);  // Thời gian
        sheet.setColumnWidth(1, 4000);  // Số đơn
        sheet.setColumnWidth(2, 5500);  // Doanh thu
        sheet.setColumnWidth(3, 5500);  // Chi phí
        sheet.setColumnWidth(4, 5500);  // Lợi nhuận
        sheet.setColumnWidth(5, 3500);  // Tăng trưởng

        int rowNum = 0;

        // Title
        sheet.createRow(rowNum++);
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📈 BÁO CÁO DOANH THU CHI TIẾT");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 5));

        rowNum++;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(28);
        String[] headers = {"📅 Thời Gian", "🛒 Đơn Hàng", "💰 Doanh Thu", "💸 Chi Phí", "✨ Lợi Nhuận", "📊 Tăng Trưởng"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        double totalRevenue = 0;
        double totalCost = 0;
        double totalProfit = 0;
        long totalOrders = 0;

        for (int i = 0; i < revenueTable.size(); i++) {
            RevenueTableRowVm row = revenueTable.get(i);
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(22);
            boolean isAlt = i % 2 == 1;

            Cell periodCell = dataRow.createCell(0);
            periodCell.setCellValue(row.periodLabel());
            periodCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell ordersCell = dataRow.createCell(1);
            ordersCell.setCellValue(row.orderCount() != null ? row.orderCount() : 0);
            ordersCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell revenueCell = dataRow.createCell(2);
            revenueCell.setCellValue(formatCurrency(row.revenue()) + " đ");
            revenueCell.setCellStyle(isAlt ? currencyStyleAlt : currencyStyle);

            Cell costCell = dataRow.createCell(3);
            costCell.setCellValue(formatCurrency(row.cost()) + " đ");
            costCell.setCellStyle(isAlt ? currencyStyleAlt : currencyStyle);

            Cell profitCell = dataRow.createCell(4);
            profitCell.setCellValue(formatCurrency(row.profit()) + " đ");
            profitCell.setCellStyle(isAlt ? currencyStyleAlt : currencyStyle);

            Cell growthCell = dataRow.createCell(5);
            growthCell.setCellValue(formatGrowth(row.growthPercent()));
            growthCell.setCellStyle(row.growthPercent() != null && row.growthPercent() >= 0 ? positiveStyle : negativeStyle);

            totalRevenue += row.revenue() != null ? row.revenue() : 0;
            totalCost += row.cost() != null ? row.cost() : 0;
            totalProfit += row.profit() != null ? row.profit() : 0;
            totalOrders += row.orderCount() != null ? row.orderCount() : 0;
        }

        // Summary row
        rowNum++;
        Row summaryRow = sheet.createRow(rowNum);
        summaryRow.setHeightInPoints(28);

        Cell summaryLabel = summaryRow.createCell(0);
        summaryLabel.setCellValue("🏆 TỔNG CỘNG");
        summaryLabel.setCellStyle(summaryStyle);

        Cell summaryOrders = summaryRow.createCell(1);
        summaryOrders.setCellValue(totalOrders);
        summaryOrders.setCellStyle(summaryStyle);

        Cell summaryRevenue = summaryRow.createCell(2);
        summaryRevenue.setCellValue(formatCurrency(totalRevenue) + " đ");
        summaryRevenue.setCellStyle(summaryStyle);

        Cell summaryCost = summaryRow.createCell(3);
        summaryCost.setCellValue(formatCurrency(totalCost) + " đ");
        summaryCost.setCellStyle(summaryStyle);

        Cell summaryProfit = summaryRow.createCell(4);
        summaryProfit.setCellValue(formatCurrency(totalProfit) + " đ");
        summaryProfit.setCellStyle(summaryStyle);

        Cell summaryGrowth = summaryRow.createCell(5);
        summaryGrowth.setCellValue("-");
        summaryGrowth.setCellStyle(summaryStyle);
    }

    private void createTopBooksSheet(XSSFWorkbook workbook, List<BookSalesVm> topBooks,
                                      CellStyle titleStyle, CellStyle headerStyle,
                                      CellStyle dataStyle, CellStyle dataStyleAlt,
                                      CellStyle currencyStyle, CellStyle currencyStyleAlt) {
        XSSFSheet sheet = workbook.createSheet("🏆 Sách Bán Chạy");

        // Column widths
        sheet.setColumnWidth(0, 1500);  // Rank
        sheet.setColumnWidth(1, 10000); // Title
        sheet.setColumnWidth(2, 6000);  // Author
        sheet.setColumnWidth(3, 3500);  // Sold
        sheet.setColumnWidth(4, 5500);  // Revenue

        int rowNum = 0;

        // Title
        sheet.createRow(rowNum++);
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("🏆 TOP 10 SÁCH BÁN CHẠY NHẤT");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 4));

        rowNum++;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(28);
        String[] headers = {"#", "📚 Tên Sách", "✍️ Tác Giả", "📦 Đã Bán", "💰 Doanh Thu"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (int i = 0; i < topBooks.size(); i++) {
            BookSalesVm book = topBooks.get(i);
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(22);
            boolean isAlt = i % 2 == 1;

            // Rank with medal emoji for top 3
            Cell rankCell = dataRow.createCell(0);
            String rankText = (i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : String.valueOf(i + 1));
            rankCell.setCellValue(rankText);
            rankCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell titleCell2 = dataRow.createCell(1);
            titleCell2.setCellValue(book.title());
            titleCell2.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell authorCell = dataRow.createCell(2);
            authorCell.setCellValue(book.author());
            authorCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell soldCell = dataRow.createCell(3);
            soldCell.setCellValue(book.soldCount());
            soldCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell revenueCell = dataRow.createCell(4);
            revenueCell.setCellValue(formatCurrency(book.revenue()) + " đ");
            revenueCell.setCellStyle(isAlt ? currencyStyleAlt : currencyStyle);
        }
    }

    private void createCategorySheet(XSSFWorkbook workbook, List<CategoryRevenueVm> categoryRevenue,
                                      CellStyle titleStyle, CellStyle headerStyle,
                                      CellStyle dataStyle, CellStyle dataStyleAlt,
                                      CellStyle currencyStyle, CellStyle currencyStyleAlt) {
        XSSFSheet sheet = workbook.createSheet("📂 Theo Danh Mục");

        // Column widths
        sheet.setColumnWidth(0, 1500);  // Rank
        sheet.setColumnWidth(1, 8000);  // Category
        sheet.setColumnWidth(2, 6000);  // Revenue
        sheet.setColumnWidth(3, 4000);  // Percentage

        int rowNum = 0;

        // Title
        sheet.createRow(rowNum++);
        Row titleRow = sheet.createRow(rowNum++);
        titleRow.setHeightInPoints(30);
        Cell titleCell = titleRow.createCell(0);
        titleCell.setCellValue("📂 DOANH THU THEO DANH MỤC");
        titleCell.setCellStyle(titleStyle);
        sheet.addMergedRegion(new CellRangeAddress(rowNum-1, rowNum-1, 0, 3));

        rowNum++;

        // Header row
        Row headerRow = sheet.createRow(rowNum++);
        headerRow.setHeightInPoints(28);
        String[] headers = {"#", "📁 Danh Mục", "💰 Doanh Thu", "📊 Tỷ Lệ"};
        for (int i = 0; i < headers.length; i++) {
            Cell cell = headerRow.createCell(i);
            cell.setCellValue(headers[i]);
            cell.setCellStyle(headerStyle);
        }

        // Data rows
        for (int i = 0; i < categoryRevenue.size(); i++) {
            CategoryRevenueVm category = categoryRevenue.get(i);
            Row dataRow = sheet.createRow(rowNum++);
            dataRow.setHeightInPoints(22);
            boolean isAlt = i % 2 == 1;

            Cell rankCell = dataRow.createCell(0);
            rankCell.setCellValue(i + 1);
            rankCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell nameCell = dataRow.createCell(1);
            nameCell.setCellValue(category.categoryName());
            nameCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);

            Cell revenueCell = dataRow.createCell(2);
            revenueCell.setCellValue(formatCurrency(category.revenue()) + " đ");
            revenueCell.setCellStyle(isAlt ? currencyStyleAlt : currencyStyle);

            Cell percentCell = dataRow.createCell(3);
            percentCell.setCellValue(category.percentage() + "%");
            percentCell.setCellStyle(isAlt ? dataStyleAlt : dataStyle);
        }
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // STYLE FACTORIES
    // ═══════════════════════════════════════════════════════════════════════════

    private CellStyle createTitleStyle(XSSFWorkbook workbook, XSSFColor tealColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 18);
        font.setColor(tealColor);
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createSubtitleStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createSectionHeaderStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 12);
        font.setColor(new XSSFColor(new byte[]{(byte)13, (byte)148, (byte)136}, null));
        style.setFont(font);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        return style;
    }

    private CellStyle createHeaderStyle(XSSFWorkbook workbook, XSSFColor bgColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        font.setColor(IndexedColors.WHITE.getIndex());
        style.setFont(font);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createDataStyle(XSSFWorkbook workbook) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        // Set border color using XSSFCellStyle method
        XSSFColor borderColor = new XSSFColor(new byte[]{(byte)229, (byte)231, (byte)235}, null);
        style.setBottomBorderColor(borderColor);
        style.setTopBorderColor(borderColor);
        style.setLeftBorderColor(borderColor);
        style.setRightBorderColor(borderColor);
        return style;
    }

    private CellStyle createDataStyleAlt(XSSFWorkbook workbook, XSSFColor bgColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createCurrencyStyle(XSSFWorkbook workbook) {
        CellStyle style = createDataStyle(workbook);
        style.setAlignment(HorizontalAlignment.RIGHT);
        Font font = workbook.createFont();
        font.setColor(IndexedColors.TEAL.getIndex());
        font.setBold(true);
        style.setFont(font);
        return style;
    }

    private CellStyle createCurrencyStyleAlt(XSSFWorkbook workbook, XSSFColor bgColor) {
        XSSFCellStyle style = (XSSFCellStyle) createCurrencyStyle(workbook);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        return style;
    }

    private CellStyle createGrowthStyle(XSSFWorkbook workbook, boolean positive) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 10);
        if (positive) {
            font.setColor(new XSSFColor(new byte[]{(byte)22, (byte)163, (byte)74}, null)); // Green
        } else {
            font.setColor(new XSSFColor(new byte[]{(byte)220, (byte)38, (byte)38}, null)); // Red
        }
        style.setFont(font);
        style.setAlignment(HorizontalAlignment.CENTER);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.THIN);
        style.setBorderTop(BorderStyle.THIN);
        style.setBorderLeft(BorderStyle.THIN);
        style.setBorderRight(BorderStyle.THIN);
        return style;
    }

    private CellStyle createSummaryStyle(XSSFWorkbook workbook, XSSFColor bgColor) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 11);
        style.setFont(font);
        style.setFillForegroundColor(bgColor);
        style.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        style.setVerticalAlignment(VerticalAlignment.CENTER);
        style.setBorderBottom(BorderStyle.MEDIUM);
        style.setBorderTop(BorderStyle.MEDIUM);
        style.setBorderLeft(BorderStyle.MEDIUM);
        style.setBorderRight(BorderStyle.MEDIUM);
        return style;
    }

    private CellStyle createKpiLabelStyle(XSSFWorkbook workbook) {
        CellStyle style = workbook.createCellStyle();
        Font font = workbook.createFont();
        font.setFontHeightInPoints((short) 10);
        font.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
        style.setFont(font);
        return style;
    }

    private CellStyle createKpiValueStyle(XSSFWorkbook workbook, XSSFColor color) {
        XSSFCellStyle style = workbook.createCellStyle();
        XSSFFont font = workbook.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        font.setColor(color);
        style.setFont(font);
        return style;
    }

    // ═══════════════════════════════════════════════════════════════════════════
    // HELPER METHODS
    // ═══════════════════════════════════════════════════════════════════════════

    private void createKpiCell(Row row, int col, String value, CellStyle style) {
        Cell cell = row.createCell(col);
        cell.setCellValue(value);
        cell.setCellStyle(style);
    }

    private void createSummaryRow(Sheet sheet, int rowNum, String label, String value, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        Cell labelCell = row.createCell(1);
        labelCell.setCellValue(label);
        labelCell.setCellStyle(style);
        Cell valueCell = row.createCell(2);
        valueCell.setCellValue(value);
        valueCell.setCellStyle(style);
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
        String arrow = value >= 0 ? "↑" : "↓";
        return arrow + " " + Math.abs(value) + "% so với kỳ trước";
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
}
