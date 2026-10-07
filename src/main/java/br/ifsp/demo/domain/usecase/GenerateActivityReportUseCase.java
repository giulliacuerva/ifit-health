package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.EnrollmentActivity;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.dto.ActivityReport;
import br.ifsp.demo.domain.usecase.dto.ActivityReportItem;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class GenerateActivityReportUseCase {

    private final EnrollmentRepository enrollmentRepository;
    private final ActivityClassRepository activityClassRepository;

    public GenerateActivityReportUseCase(
            EnrollmentRepository enrollmentRepository,
            ActivityClassRepository activityClassRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.activityClassRepository = activityClassRepository;
    }

    private void calculateAverageOccupancy(
            ActivityReportItem item,
            List<EnrollmentActivity> enrollmentActivities,
            LocalDate startDate,
            LocalDate endDate) {

        List<LocalDate> changeDates = new ArrayList<>();

        changeDates.add(startDate);

        for (EnrollmentActivity enrollmentActivity : enrollmentActivities) {
            LocalDate enrollmentStart = enrollmentActivity.getStartDate();

            if (enrollmentStart.isAfter(startDate)
                    && !enrollmentStart.isAfter(endDate)) {
                changeDates.add(enrollmentStart);
            }
        }

        for (LocalDate date : changeDates) {
            int activeEnrollments = (int) enrollmentActivities.stream()
                    .filter(activity ->
                            !activity.getStartDate().isAfter(date)
                    )
                    .filter(activity ->
                            activity.getEndDate() == null
                                    || activity.getEndDate().isAfter(date)
                    )
                    .count();

            item.addOccupancy(activeEnrollments);
        }

        item.calculateAverageOccupancy();
    }

    public ActivityReport generate(LocalDate startDate, LocalDate endDate) {
        List<EnrollmentActivity> enrollmentActivities = enrollmentRepository
                .findEnrollmentActivitiesByPeriod(startDate, endDate);

        List<ActivityReportItem> items = new ArrayList<>();

        for (var activityClass : activityClassRepository.findAll()) {
            items.add(new ActivityReportItem(activityClass));
        }

        for (EnrollmentActivity enrollmentActivity : enrollmentActivities) {
            ActivityReportItem item = findItem(
                    items,
                    enrollmentActivity.getActivityClass().getId()
            );

            if (item != null) {
                int activeMonths = calculateActiveMonths(
                        enrollmentActivity,
                        startDate,
                        endDate
                );

                for (int i = 0; i < activeMonths; i++) {
                    item.addEnrollment(enrollmentActivity.getMonthlyFee());
                }
            }
        }

        for (ActivityReportItem item : items) {
            calculateAverageOccupancy(
                    item,
                    enrollmentActivities,
                    startDate,
                    endDate
            );
        }

        return new ActivityReport(startDate, endDate, items);
    }

    public ActivityReport generate(
            LocalDate startDate,
            LocalDate endDate,
            Sport sport) {

        List<EnrollmentActivity> enrollmentActivities = enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndSport(
                        startDate,
                        endDate,
                        sport
                );

        return createReport(startDate, endDate, enrollmentActivities);
    }

    public ActivityReport generate(
            LocalDate startDate,
            LocalDate endDate,
            DayOfWeek dayOfWeek) {

        List<EnrollmentActivity> enrollmentActivities = enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndDayOfWeek(
                        startDate,
                        endDate,
                        dayOfWeek
                );

        return createReport(startDate, endDate, enrollmentActivities);
    }

    public ActivityReport generate(
            LocalDate startDate,
            LocalDate endDate,
            Sport sport,
            DayOfWeek dayOfWeek) {

        List<EnrollmentActivity> enrollmentActivities = enrollmentRepository
                .findActiveEnrollmentActivitiesByStartDateBetweenAndSport(
                        startDate,
                        endDate,
                        sport
                );

        enrollmentActivities = filterByDayOfWeek(
                enrollmentActivities,
                dayOfWeek
        );

        return createReport(
                startDate,
                endDate,
                enrollmentActivities
        );
    }

    private List<EnrollmentActivity> filterByDayOfWeek(
            List<EnrollmentActivity> enrollmentActivities,
            DayOfWeek dayOfWeek) {

        return enrollmentActivities.stream()
                .filter(enrollmentActivity ->
                        enrollmentActivity.getActivityClass()
                                .getSchedule()
                                .getWeekdays()
                                .contains(dayOfWeek))
                .toList();
    }

    private ActivityReport createReport(
            LocalDate startDate,
            LocalDate endDate,
            List<EnrollmentActivity> enrollmentActivities) {

        return getActivityReport(
                startDate,
                endDate,
                enrollmentActivities
        );
    }

    private ActivityReport getActivityReport(
            LocalDate startDate,
            LocalDate endDate,
            List<EnrollmentActivity> enrollmentActivities) {

        List<ActivityReportItem> items = new ArrayList<>();

        for (EnrollmentActivity enrollmentActivity : enrollmentActivities) {
            ActivityReportItem item = findItem(
                    items,
                    enrollmentActivity.getActivityClass().getId()
            );

            if (item == null) {
                item = new ActivityReportItem(
                        enrollmentActivity.getActivityClass()
                );

                items.add(item);
            }

            item.addEnrollment(
                    enrollmentActivity.getMonthlyFee()
            );
        }

        return new ActivityReport(
                startDate,
                endDate,
                items
        );
    }

    private int calculateActiveMonths(
            EnrollmentActivity enrollmentActivity,
            LocalDate startDate,
            LocalDate endDate) {

        LocalDate activeStart = enrollmentActivity.getStartDate();

        LocalDate activeEnd = enrollmentActivity.getEndDate() != null
                ? enrollmentActivity.getEndDate()
                : endDate;

        if (activeStart.isBefore(startDate)) {
            activeStart = startDate;
        }

        if (activeEnd.isAfter(endDate)) {
            activeEnd = endDate;
        }

        return (int) ChronoUnit.MONTHS.between(
                activeStart.withDayOfMonth(1),
                activeEnd.withDayOfMonth(1)
        ) + 1;
    }

    private ActivityReportItem findItem(
            List<ActivityReportItem> items,
            UUID activityClassId) {

        return items.stream()
                .filter(item ->
                        item.getActivityClass()
                                .getId()
                                .equals(activityClassId))
                .findFirst()
                .orElse(null);
    }
}