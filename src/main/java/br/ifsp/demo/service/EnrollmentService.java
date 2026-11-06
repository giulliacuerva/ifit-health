package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.CustomerRepository;
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
    private final CustomerRepository customerRepository;
    private final ActivityClassRepository activityClassRepository;
    private final EnrollCustomerUseCase enrollUseCase;
    private final CancelEnrollmentActivityUseCase cancelUseCase;

    public EnrollmentService(CustomerRepository customerRepository,
                             ActivityClassRepository activityClassRepository,
                             EnrollCustomerUseCase enrollUseCase,
                             CancelEnrollmentActivityUseCase cancelUseCase) {
        this.customerRepository = customerRepository;
        this.activityClassRepository = activityClassRepository;
        this.enrollUseCase = enrollUseCase;
        this.cancelUseCase = cancelUseCase;
    }

    public Enrollment enroll(UUID customerId, UUID authenticatedUserId,
                             List<UUID> activityClassIds) {
        Customer customer = findCustomerOwnedByAuthenticatedUser(customerId, authenticatedUserId);

        for (UUID activityClassId : activityClassIds) {
            if (activityClassRepository.findById(activityClassId) == null) {
                throw new ResourceNotFoundException("Activity class not found: " + activityClassId);
            }
        }

        return enrollUseCase.enroll(customer, activityClassIds);
    }

    public Enrollment cancelActivity(UUID customerId, UUID authenticatedUserId,
                                     UUID enrollmentActivityId, LocalDate endDate) {
        Customer customer = findCustomerOwnedByAuthenticatedUser(customerId, authenticatedUserId);
        return cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate);
    }

    private Customer findCustomerOwnedByAuthenticatedUser(UUID customerId, UUID authenticatedUserId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));

        if (authenticatedUserId == null || !authenticatedUserId.equals(customer.getUserId())) {
            throw new AccessDeniedException(
                    "You are not allowed to manage enrollments for this customer."
            );
        }
        return customer;
    }
}
