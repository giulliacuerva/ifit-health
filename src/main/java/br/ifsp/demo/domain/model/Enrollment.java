package br.ifsp.demo.domain.model;

import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.exception.EnrollmentActivityNotFoundException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Enrollment {
    private final UUID id;
    private final Customer customer;
    private final List<EnrollmentActivity> enrollmentActivities;
    private boolean active;

    public Enrollment(Customer customer) {
        this.id = UUID.randomUUID();
        this.customer = customer;
        this.enrollmentActivities = new ArrayList<>();
        this.active = true;
    }

    public void addActivity(ActivityClass activityClass) {
        if (hasActivity(activityClass)){
            throw new IllegalStateException("The customer is already enrolled in this activity");
        }
        if (hasScheduleConflict(activityClass)) {
            throw new ActivityScheduleConflictException(
                    "The activity conflicts with the customer's schedule"
            );
        }
        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(
                activityClass, activityClass.getMonthlyFee()
        );
        enrollmentActivities.add(enrollmentActivity);

        if (!active) { activate(); }
    }

    private boolean hasActivity(ActivityClass activityClass) {
        return enrollmentActivities.stream()
                .anyMatch(enrollmentActivity ->
                        enrollmentActivity.isActive()
                        && enrollmentActivity.getActivityClass().getId().equals(activityClass.getId()));
    }

    private boolean hasScheduleConflict(ActivityClass activityClass) {
        return enrollmentActivities.stream()
                .anyMatch(enrollmentActivity ->
                        enrollmentActivity.isActive()
                                && enrollmentActivity.getActivityClass()
                                .getSchedule().conflictsWith(activityClass.getSchedule()));
    }

    public void cancelActivity(UUID activityId) {
        EnrollmentActivity enrollmentActivity = enrollmentActivities.stream()
                .filter(activity -> activity.getId()
                        .equals(activityId))
                .findFirst().orElseThrow(() -> new EnrollmentActivityNotFoundException("Enrollment activity not found"));

        enrollmentActivity.deactivate();
    }

    public boolean isActive() { return active; }

    public void activate() { this.active = true; }

    public void deactivate() { this.active = false; }

    public UUID getId() { return id; }

    public Customer getCustomer() { return customer; }

    public List<EnrollmentActivity> getEnrollmentActivities() {
        return List.copyOf(enrollmentActivities);
    }
}
