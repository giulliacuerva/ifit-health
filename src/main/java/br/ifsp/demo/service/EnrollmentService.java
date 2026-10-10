package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {
    private final EnrollmentRepository enrollmentRepository;
    private final ActivityClassRepository activityClassRepository;
    private final EnrollCustomerUseCase enrollUseCase;
    private final CancelEnrollmentActivityUseCase cancelUseCase;

    public EnrollmentService(EnrollmentRepository enrollmentRepository,
                             ActivityClassRepository activityClassRepository,
                             EnrollCustomerUseCase enrollUseCase,
                             CancelEnrollmentActivityUseCase cancelUseCase) {
        this.enrollmentRepository = enrollmentRepository;
        this.activityClassRepository = activityClassRepository;
        this.enrollUseCase = enrollUseCase;
        this.cancelUseCase = cancelUseCase;
    }

    public Enrollment enroll(UUID customerId, UUID authenticatedUserId,
                             List<UUID> activityClassIds) {
        var customer = enrollmentRepository.findCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        checkCustomerOwner(customer.getUserId(), authenticatedUserId);

        for (UUID activityClassId : activityClassIds) {
            if (activityClassRepository.findById(activityClassId) == null) {
                throw new ResourceNotFoundException("Activity class not found: " + activityClassId);
            }
        }

        return enrollUseCase.enroll(customer, activityClassIds);
    }

    public Enrollment cancelActivity(UUID customerId, UUID authenticatedUserId,
                                     UUID enrollmentActivityId, LocalDate endDate) {
        var customer = enrollmentRepository.findCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        checkCustomerOwner(customer.getUserId(), authenticatedUserId);
        return cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate);
    }

    private void checkCustomerOwner(UUID customerUserId, UUID authenticatedUserId) {
        if (customerUserId == null || !customerUserId.equals(authenticatedUserId)) {
            throw new AccessDeniedException("The authenticated user does not own this customer");
        }
    }
}
