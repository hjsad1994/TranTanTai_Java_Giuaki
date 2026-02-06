package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Revenue chart data for Chart.js integration.
 */
@Schema(description = "Revenue chart data")
public record RevenueChartVm(
    @Schema(description = "Chart labels (time periods)", example = "[\"T1\", \"T2\", \"T3\"]")
    List<String> labels,
    
    @Schema(description = "Revenue values for each period", example = "[32000000, 38000000, 35000000]")
    List<Double> data
) {}
