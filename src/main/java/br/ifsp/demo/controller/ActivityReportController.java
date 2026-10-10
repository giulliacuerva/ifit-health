package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityReportResponse;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.GenerateActivityReportUseCase;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-reports")
public class ActivityReportController {
    private final GenerateActivityReportUseCase reports;
    private final ActivityClassRepository repository;

    public ActivityReportController(GenerateActivityReportUseCase reports,
                                    ActivityClassRepository repository) {
        this.reports = reports;
        this.repository = repository;
    }

    @GetMapping
    public ActivityReportResponse report(@RequestParam LocalDate startDate,
                                         @RequestParam LocalDate endDate,
                                         @RequestParam(required = false) UUID sportId,
                                         @RequestParam(required = false) DayOfWeek dayOfWeek) {
        br.ifsp.demo.domain.usecase.dto.ActivityReport report;
        if (sportId != null && dayOfWeek == null) {
            var sport = repository.findSportById(sportId);
            if (sport == null) {
                throw new ResourceNotFoundException("Sport not found: " + sportId);
            }
            report = reports.generate(startDate, endDate, sport);
        } else if (sportId == null && dayOfWeek != null) {
            report = reports.generate(startDate, endDate, dayOfWeek);
        } else if (sportId != null) {
            var sport = repository.findSportById(sportId);
            if (sport == null) {
                throw new ResourceNotFoundException("Sport not found: " + sportId);
            }
            report = reports.generate(startDate, endDate, sport, dayOfWeek);
        } else {
            report = reports.generate(startDate, endDate);
        }
        return ActivityReportResponse.from(report);
    }
}
