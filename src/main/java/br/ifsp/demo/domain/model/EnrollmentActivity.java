package br.ifsp.demo.domain.model;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;

public class EnrollmentActivity {
    private final UUID id;
    private final ActivityClass activityClass;
    private final BigDecimal monthlyFee;

    public EnrollmentActivity(ActivityClass activityClass, BigDecimal monthlyFee) {
        this.id = UUID.randomUUID();
        this.activityClass = Objects.requireNonNull(activityClass);
        this.monthlyFee = Objects.requireNonNull(monthlyFee);
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
