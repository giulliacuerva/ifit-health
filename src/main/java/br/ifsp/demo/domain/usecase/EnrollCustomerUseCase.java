package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.EnrollmentActivity;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;

import java.util.List;
import java.util.UUID;

public class EnrollCustomerUseCase {
    private final ActivityClassRepository activityClassRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollCustomerUseCase(ActivityClassRepository activityClassRepository,
                                 EnrollmentRepository enrollmentRepository) {
        this.activityClassRepository = activityClassRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Enrollment enroll(Customer customer, List<UUID> activityClassIds) {
        Enrollment enrollment = enrollmentRepository.findByCustomer(customer);

        for (UUID activityClassId : activityClassIds) {
            ActivityClass activityClass = activityClassRepository.findById(activityClassId);

            List<EnrollmentActivity> enrollmentActivities =
                    enrollmentRepository.findActivitiesByActivityClass(activityClass);
            if ((enrollmentActivities.size() >= activityClass.getCapacity())){
                throw new IllegalStateException("The activity is currently full");
            }

            enrollment.addActivity(activityClass);
        }
        return enrollmentRepository.save(enrollment);
    }
}
