package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.EnrollmentActivity;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.CapacityIsGreaterThanAcceptedException;

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

        List<ActivityClass> activities = activityClassIds.stream()
                .map(activityClassRepository::findById)
                .toList();

        for (ActivityClass activity : activities) {
            if (!activity.isActive()) {
                throw new IllegalStateException("The activity is inactive");
            }
            List<EnrollmentActivity> enrolled = enrollmentRepository.findActivitiesByActivityClass(activity);

            if (enrolled.size() >= activity.getCapacity()) {
                throw new CapacityIsGreaterThanAcceptedException("The activity has no available vacancies");
            }
        }
        for (ActivityClass activity : activities) {enrollment.addActivity(activity);}

        return enrollmentRepository.save(enrollment);
    }
}
