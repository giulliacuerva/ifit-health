package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.GenerateActivityReportUseCase;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.UUID;

@Service
public class ActivityReportService {
    private final ActivityClassRepository activityClassRepository;
    private final GenerateActivityReportUseCase reportUseCase;

    public ActivityReportService(ActivityClassRepository activityClassRepository,
                                 GenerateActivityReportUseCase reportUseCase) {
        this.activityClassRepository = activityClassRepository;
        this.reportUseCase = reportUseCase;
    }

    public ActivityReport generate(LocalDate startDate, LocalDate endDate,
                                   UUID sportId, DayOfWeek dayOfWeek) {
        if (sportId == null && dayOfWeek == null) {
            return reportUseCase.generate(startDate, endDate);
        }

        if (sportId == null) {
            return reportUseCase.generate(startDate, endDate, dayOfWeek);
        }

        Sport sport = activityClassRepository.findSportById(sportId);
        if (sport == null) {
            throw new ResourceNotFoundException("Sport not found: " + sportId);
        }

        if (dayOfWeek == null) {
            return reportUseCase.generate(startDate, endDate, sport);
        }

        return reportUseCase.generate(startDate, endDate, sport, dayOfWeek);
    }
}
