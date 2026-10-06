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
        this.id = UUID.randomUUID();
        this.activityClass = Objects.requireNonNull(activityClass);
        this.monthlyFee = Objects.requireNonNull(monthlyFee);
        this.active = true;
        this.startDate = LocalDate.now();
    }

    public void deactivate() {
        if (!active) {
            throw new EnrollmentActivityAlreadyInactiveException("Enrollment activity is already inactive");
        }
        this.endDate = LocalDate.now();
        this.active = false;
    }

    public boolean isActive() { return active; }

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
        return Objects.equals(id, that.id) && Objects.equals(activityClass, that.activityClass) && Objects.equals(monthlyFee, that.monthlyFee);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, activityClass, monthlyFee);
    }
}
