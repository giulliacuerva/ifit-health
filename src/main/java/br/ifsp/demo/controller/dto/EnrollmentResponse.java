package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.Enrollment;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record EnrollmentResponse(
        UUID id,
        UUID customerId,
        boolean active,
        List<ActivityEnrollment> activities
) {
    public record ActivityEnrollment(UUID enrollmentActivityId, UUID activityClassId,
                                     BigDecimal monthlyFee, boolean active,
                                     LocalDate startDate, LocalDate endDate) {}

    public static EnrollmentResponse from(Enrollment enrollment) {
        var activities = enrollment.getEnrollmentActivities().stream()
                .map(a -> new ActivityEnrollment(a.getId(), a.getActivityClass().getId(),
                        a.getMonthlyFee(), a.isActive(), a.getStartDate(), a.getEndDate()))
                .toList();
        return new EnrollmentResponse(enrollment.getId(), enrollment.getCustomer().getId(),
                enrollment.isActive(), activities);
    }
}
