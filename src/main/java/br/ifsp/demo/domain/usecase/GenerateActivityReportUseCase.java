package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.EnrollmentActivity;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.domain.usecase.dto.ActivityReportItem;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateActivityReportUseCase {

    private final EnrollmentRepository enrollmentRepository;

    public GenerateActivityReportUseCase(EnrollmentRepository enrollmentRepository) {
        this.enrollmentRepository = enrollmentRepository;
    }

    public ActivityReport generate(LocalDate startDate, LocalDate endDate) {
        List<EnrollmentActivity> enrollmentActivities = enrollmentRepository
                        .findActiveEnrollmentActivitiesByStartDateBetween(startDate, endDate);
        List<ActivityReportItem> items = new ArrayList<>();

        for (EnrollmentActivity enrollmentActivity : enrollmentActivities) {
            ActivityReportItem item = findItem(items, enrollmentActivity.getActivityClass().getId());
            if (item == null) {
                item = new ActivityReportItem(enrollmentActivity.getActivityClass());
                items.add(item);
            }
            item.addEnrollment(enrollmentActivity.getMonthlyFee());
        }
        return new ActivityReport(startDate, endDate, items);
    }

    private ActivityReportItem findItem(List<ActivityReportItem> items, UUID activityClassId) {
        return items.stream()
                .filter(item -> item.getActivityClass().getId().equals(activityClassId))
                .findFirst()
                .orElse(null);
    }
}