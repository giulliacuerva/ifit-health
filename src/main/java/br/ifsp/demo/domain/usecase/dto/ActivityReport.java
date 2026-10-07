package br.ifsp.demo.domain.usecase.dto;

import java.time.LocalDate;
import java.util.List;

public class ActivityReport {

    private final LocalDate startDate;
    private final LocalDate endDate;
    private final List<ActivityReportItem> items;

    public ActivityReport(LocalDate startDate, LocalDate endDate, List<ActivityReportItem> items) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.items = items;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<ActivityReportItem> getItems() {
        return items;
    }
}