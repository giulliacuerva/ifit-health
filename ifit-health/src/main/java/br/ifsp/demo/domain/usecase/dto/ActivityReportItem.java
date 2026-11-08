package br.ifsp.demo.domain.usecase.dto;

import br.ifsp.demo.domain.model.ActivityClass;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ActivityReportItem {

    private final ActivityClass activityClass;
    private int enrollmentCount;
    private BigDecimal revenue;
    private BigDecimal averageOccupancy;
    private BigDecimal occupancySum;
    private int occupancySamples;

    public ActivityReportItem(ActivityClass activityClass) {
        this.activityClass = activityClass;
        this.enrollmentCount = 0;
        this.revenue = BigDecimal.ZERO;
        this.averageOccupancy = BigDecimal.ZERO;
        this.occupancySum = BigDecimal.ZERO;
        this.occupancySamples = 0;
    }

    public void addEnrollment(BigDecimal monthlyFee) {
        enrollmentCount++;
        revenue = revenue.add(monthlyFee);
    }

    public void addOccupancy(int enrollmentCount) {
        occupancySum = occupancySum.add(
                BigDecimal.valueOf(enrollmentCount)
        );
        occupancySamples++;
    }

    public void calculateAverageOccupancy() {
        if (activityClass.getCapacity() == 0 || occupancySamples == 0) {
            averageOccupancy = BigDecimal.ZERO;
            return;
        }

        averageOccupancy = occupancySum
                .multiply(BigDecimal.valueOf(100))
                .divide(
                        BigDecimal.valueOf(
                                activityClass.getCapacity() * occupancySamples
                        ),
                        2,
                        RoundingMode.HALF_UP
                );
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

    public BigDecimal getAverageOccupancy() {
        return averageOccupancy;
    }
}