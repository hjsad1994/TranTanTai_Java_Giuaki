package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Revenue table row data for detailed reports.
 */
@Schema(description = "Revenue table row")
public record RevenueTableRowVm(
    @Schema(description = "Period identifier (YYYY-MM-DD or YYYY-WXX or YYYY-MM)", example = "2024-01")
    String period,
    
    @Schema(description = "Human readable period label", example = "Thang 01/2024")
    String periodLabel,
    
    @Schema(description = "Number of orders in this period", example = "156")
    Long orderCount,
    
    @Schema(description = "Total revenue in this period", example = "45680000")
    Double revenue,
    
    @Schema(description = "Estimated cost (70% of revenue)", example = "31976000")
    Double cost,
    
    @Schema(description = "Estimated profit (30% of revenue)", example = "13704000")
    Double profit,
    
    @Schema(description = "Growth percentage vs previous period", example = "15.2")
    Double growthPercent
) {}
