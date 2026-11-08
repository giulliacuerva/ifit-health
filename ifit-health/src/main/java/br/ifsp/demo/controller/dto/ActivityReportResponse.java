package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Activity report response for the requested date range.")
public record ActivityReportResponse(
        @Schema(description = "First day of the report period.", example = "2026-01-01")
        LocalDate startDate,
        @Schema(description = "Last day of the report period.", example = "2026-01-31")
        LocalDate endDate,
        @Schema(description = "Report items returned by the current report calculation.")
        List<Item> items
) {
    @Schema(description = "Aggregated values for one activity class.")
    public record Item(
            @Schema(description = "Activity class UUID.")
            UUID activityClassId,
            @Schema(description = "Activity capacity.", example = "20")
            int capacity,
            @Schema(description = "Enrollment count as accumulated by the current report calculation.", example = "12")
            int enrollmentCount,
            @Schema(description = "Revenue amount calculated for the report period, in BRL.", example = "1800.00")
            BigDecimal revenue,
            @Schema(description = "Average occupancy percentage returned by the report calculation.", example = "60.00")
            BigDecimal averageOccupancyPercent
    ) {
    }

    public static ActivityReportResponse from(ActivityReport report) {
        return new ActivityReportResponse(report.getStartDate(), report.getEndDate(),
                report.getItems().stream().map(item -> new Item(
                        item.getActivityClass().getId(), item.getActivityClass().getCapacity(),
                        item.getEnrollmentCount(), item.getRevenue(),
                        item.getAverageOccupancy())).toList());
    }
}
