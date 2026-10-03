package br.ifsp.demo.domain.model;

import br.ifsp.demo.exception.ActivityScheduleConflictException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Enrollment {
    private final UUID id;
    private final Customer customer;
    private final List<EnrollmentActivity> enrollmentActivities;

    public Enrollment(Customer customer) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.enrollmentActivities = new ArrayList<>();
    }

    public void addActivity(ActivityClass activityClass) {
        if (hasScheduleConflict(activityClass)) {
            throw new ActivityScheduleConflictException(
                    "The activity conflicts with the customer's schedule"
            );
        }
        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(
                activityClass, activityClass.getMonthlyFee()
        );
        enrollmentActivities.add(enrollmentActivity);
    }

    private boolean hasScheduleConflict(ActivityClass activityClass) {
        return enrollmentActivities.stream()
                .anyMatch(enrollmentActivity ->
                        enrollmentActivity
                                .getActivityClass()
                                .getSchedule()
                                .conflictsWith(activityClass.getSchedule())
                );
    }

    public UUID getId() {
        return id;
    }

    public Customer getCustomer() {
        return customer;
    }

    public List<EnrollmentActivity> getEnrollmentActivities() {
        return List.copyOf(enrollmentActivities);
    }
}
