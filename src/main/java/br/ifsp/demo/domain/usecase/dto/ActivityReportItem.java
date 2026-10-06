package br.ifsp.demo.domain.usecase.dto;

import br.ifsp.demo.domain.model.ActivityClass;

import java.math.BigDecimal;

public class ActivityReportItem {
    private final ActivityClass activityClass;
    private int enrollmentCount;
    private BigDecimal revenue;

    public ActivityReportItem(ActivityClass activityClass) {
        this.activityClass = activityClass;
        this.enrollmentCount = 0;
        this.revenue = BigDecimal.ZERO;
    }

    public void addEnrollment(BigDecimal monthlyFee) {
        enrollmentCount++;
        revenue = revenue.add(monthlyFee);
    }

    public ActivityClass getActivityClass() {
        return activityClass;
    }

    public int getEnrollmentCount() {
        return enrollmentCount;
    }

    public BigDecimal getRevenue() {
        return revenue;
    }
}