package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record ActivityReportResponse(LocalDate startDate, LocalDate endDate, List<Item> items) {
    public record Item(UUID activityClassId, int capacity, int enrollmentCount,
                       BigDecimal revenue, BigDecimal averageOccupancyPercent) {}

    public static ActivityReportResponse from(ActivityReport report) {
        return new ActivityReportResponse(report.getStartDate(), report.getEndDate(),
                report.getItems().stream().map(item -> new Item(
                        item.getActivityClass().getId(), item.getActivityClass().getCapacity(),
                        item.getEnrollmentCount(), item.getRevenue(),
                        item.getAverageOccupancy())).toList());
    }
}
