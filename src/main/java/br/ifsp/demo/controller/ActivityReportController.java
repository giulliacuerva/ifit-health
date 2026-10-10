package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityReportResponse;
import br.ifsp.demo.service.ActivityReportService;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-reports")
public class ActivityReportController {
    private final ActivityReportService reportService;

    public ActivityReportController(ActivityReportService reportService) {
        this.reportService = reportService;
    }

    @GetMapping
    public ActivityReportResponse report(@RequestParam LocalDate startDate,
                                         @RequestParam LocalDate endDate,
                                         @RequestParam(required = false) UUID sportId,
                                         @RequestParam(required = false) DayOfWeek dayOfWeek) {
        var report = reportService.generate(startDate, endDate, sportId, dayOfWeek);
        return ActivityReportResponse.from(report);
    }
}
