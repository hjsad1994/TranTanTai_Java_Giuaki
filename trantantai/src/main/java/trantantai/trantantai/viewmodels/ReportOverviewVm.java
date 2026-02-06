package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Report overview data for admin dashboard.
 */
@Schema(description = "Report overview statistics")
public record ReportOverviewVm(
    @Schema(description = "Total revenue from delivered orders", example = "125450000")
    Double totalRevenue,
    
    @Schema(description = "Total number of orders", example = "1247")
    Long totalOrders,
    
    @Schema(description = "Average order value", example = "245000")
    Double avgOrderValue,
    
    @Schema(description = "Number of new customers in period", example = "156")
    Integer newCustomers,
    
    @Schema(description = "Revenue growth percentage vs previous period", example = "23.5")
    Double revenueGrowth,
    
    @Schema(description = "Orders growth percentage vs previous period", example = "18.2")
    Double ordersGrowth,
    
    @Schema(description = "Average value growth percentage vs previous period", example = "0")
    Double avgValueGrowth,
    
    @Schema(description = "New customers growth percentage vs previous period", example = "12.8")
    Double customersGrowth
) {}
