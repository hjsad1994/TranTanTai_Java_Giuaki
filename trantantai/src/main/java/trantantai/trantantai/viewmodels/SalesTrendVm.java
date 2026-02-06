package trantantai.trantantai.viewmodels;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.List;

/**
 * Sales trend data comparing current vs previous period.
 */
@Schema(description = "Sales trend comparison data")
public record SalesTrendVm(
    @Schema(description = "Chart labels (time periods)", example = "[\"T1\", \"T2\", \"T3\"]")
    List<String> labels,
    
    @Schema(description = "Current period values", example = "[28000000, 32000000, 35000000]")
    List<Double> currentPeriod,
    
    @Schema(description = "Previous period values for comparison", example = "[22000000, 25000000, 28000000]")
    List<Double> previousPeriod
) {}
