package br.ifsp.demo.domain.model;

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
        EnrollmentActivity enrollmentActivity = new EnrollmentActivity(
                activityClass, activityClass.getMonthlyFee()
        );
        enrollmentActivities.add(enrollmentActivity);
    }

    public void cancelActivity(UUID activityId) {
        EnrollmentActivity enrollmentActivity = enrollmentActivities.stream()
                .filter(activity -> activity.getId()
                        .equals(activityId))
                        .findFirst().orElseThrow();
        enrollmentActivities.remove(enrollmentActivity);
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
