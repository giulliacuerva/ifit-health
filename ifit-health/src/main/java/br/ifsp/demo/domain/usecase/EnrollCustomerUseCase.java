package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollCustomerUseCase {
    private final ActivityClassRepository activityClassRepository;
    private final EnrollmentRepository enrollmentRepository;

    public EnrollCustomerUseCase(ActivityClassRepository activityClassRepository,
                                 EnrollmentRepository enrollmentRepository) {
        this.activityClassRepository = activityClassRepository;
        this.enrollmentRepository = enrollmentRepository;
    }

    public Enrollment enroll(Customer customer, List<UUID> activityClassIds) {
        if (activityClassIds == null || activityClassIds.isEmpty()) {
            throw new IllegalArgumentException("At least one activity must be selected.");
        }
        if (new HashSet<>(activityClassIds).size() != activityClassIds.size()) {
            throw new IllegalArgumentException("The same activity cannot be selected more than once.");
        }

        List<ActivityClass> activities = activityClassIds.stream()
                .map(activityId -> {
                    ActivityClass activity = activityClassRepository.findById(activityId);
                    if (activity == null) {
                        throw new ResourceNotFoundException("Activity class not found: " + activityId);
                    }
                    if (!activity.isActive()) {
                        throw new IllegalStateException("The activity is inactive: " + activityId);
                    }
                    return activity;
                })
                .toList();

        return enrollmentRepository.enroll(customer, activities);
    }
}
