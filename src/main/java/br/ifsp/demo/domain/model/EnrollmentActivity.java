package br.ifsp.demo.domain.model;

import br.ifsp.demo.exception.EnrollmentActivityAlreadyInactiveException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class EnrollmentActivity {

    private final UUID id;
    private final ActivityClass activityClass;
    private final BigDecimal monthlyFee;
    private boolean active;
    private final LocalDate startDate;
    private LocalDate endDate;

    public EnrollmentActivity(ActivityClass activityClass, BigDecimal monthlyFee) {
        this(activityClass, monthlyFee, LocalDate.now());
    }

    public EnrollmentActivity(
            ActivityClass activityClass,
            BigDecimal monthlyFee,
            LocalDate startDate) {

        this.id = UUID.randomUUID();
        this.activityClass = Objects.requireNonNull(activityClass);
        this.monthlyFee = Objects.requireNonNull(monthlyFee);
        this.active = true;
        this.startDate = Objects.requireNonNull(startDate);
    }

    public EnrollmentActivity(UUID id, ActivityClass activityClass, BigDecimal monthlyFee, boolean active, LocalDate startDate, LocalDate endDate) {
        this.id = id;
        this.activityClass = activityClass;
        this.monthlyFee = monthlyFee;
        this.active = active;
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public void deactivate() {
        deactivate(LocalDate.now());
    }

    public void deactivate(LocalDate endDate) {
        if (!active) {
            throw new EnrollmentActivityAlreadyInactiveException(
                    "Enrollment activity is already inactive"
            );
        }

        Objects.requireNonNull(endDate, "End date cannot be null");
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date cannot be before enrollment start date");
        }
        this.endDate = endDate;
        this.active = false;
    }

    public boolean isActive() {
        return active;
    }

    public UUID getId() {
        return id;
    }

    public ActivityClass getActivityClass() {
        return activityClass;
    }

    public BigDecimal getMonthlyFee() {
        return monthlyFee;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;

        EnrollmentActivity that = (EnrollmentActivity) o;

        return Objects.equals(id, that.id)
                && Objects.equals(activityClass, that.activityClass)
                && Objects.equals(monthlyFee, that.monthlyFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, activityClass, monthlyFee);
    }
}
