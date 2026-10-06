package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.EnrollmentActivity;
import br.ifsp.demo.domain.repository.EnrollmentRepository;

import java.util.UUID;

public class CancelEnrollmentActivityUseCase {

    private final EnrollmentRepository enrollmentRepo;

    public CancelEnrollmentActivityUseCase(EnrollmentRepository enrollmentRepo) {
        this.enrollmentRepo = enrollmentRepo;
    }

    public Enrollment cancelActivity(Customer customer, UUID activityId) {
        Enrollment enrollment = enrollmentRepo.findByCustomer(customer);
        enrollment.cancelActivity(activityId);
        boolean isAllDeactivated = enrollment.getEnrollmentActivities()
                .stream()
                .noneMatch(EnrollmentActivity::isActive);
        if (isAllDeactivated) { enrollment.deactivate(); }
        return enrollmentRepo.save(enrollment);
    }
}