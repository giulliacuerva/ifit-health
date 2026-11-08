package br.ifsp.demo.controller.dto;

import br.ifsp.demo.domain.model.Enrollment;
import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Schema(description = "Enrollment information belonging to the authenticated student.")
public record EnrollmentResponse(
        @Schema(description = "Enrollment UUID.")
        UUID id,
        @Schema(description = "Customer UUID associated with this enrollment.")
        UUID customerId,
        @Schema(description = "Whether the main enrollment is active.", example = "true")
        boolean active,
        @Schema(description = "Activity enrollment records associated with the student.")
        List<ActivityEnrollment> activities
) {
    @Schema(description = "Enrollment details for one activity class, including the agreed monthly fee and dates.")
    public record ActivityEnrollment(
            @Schema(description = "Enrollment activity UUID.")
            UUID enrollmentActivityId,
            @Schema(description = "Activity class UUID.")
            UUID activityClassId,
            @Schema(description = "Monthly fee agreed at enrollment, in BRL. This value is stored in the enrollment history.",
                    example = "150.00")
            BigDecimal monthlyFee,
            @Schema(description = "Whether this activity enrollment is active.", example = "true")
            boolean active,
            @Schema(description = "Enrollment start date.", example = "2026-01-10")
            LocalDate startDate,
            @Schema(description = "Enrollment end date; null while there is no end date.",
                    example = "2026-06-30", nullable = true)
            LocalDate endDate
    ) {
    }

    public static EnrollmentResponse from(Enrollment enrollment) {
        var activities = enrollment.getEnrollmentActivities().stream()
                .map(a -> new ActivityEnrollment(a.getId(), a.getActivityClass().getId(),
                        a.getMonthlyFee(), a.isActive(), a.getStartDate(), a.getEndDate()))
                .toList();
        return new EnrollmentResponse(enrollment.getId(), enrollment.getCustomer().getId(),
                enrollment.isActive(), activities);
    }
}
