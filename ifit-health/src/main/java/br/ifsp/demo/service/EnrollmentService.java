package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.CustomerRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Service
public class EnrollmentService {
    private final CustomerRepository customerRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final EnrollCustomerUseCase enrollUseCase;
    private final CancelEnrollmentActivityUseCase cancelUseCase;

    public EnrollmentService(CustomerRepository customerRepository,
                             EnrollmentRepository enrollmentRepository,
                             EnrollCustomerUseCase enrollUseCase,
                             CancelEnrollmentActivityUseCase cancelUseCase) {
        this.customerRepository = customerRepository;
        this.enrollmentRepository = enrollmentRepository;
        this.enrollUseCase = enrollUseCase;
        this.cancelUseCase = cancelUseCase;
    }

    public Enrollment enroll(UUID authenticatedUserId, List<UUID> activityClassIds) {
        Customer customer = findCustomerByAuthenticatedUser(authenticatedUserId);
        return enrollUseCase.enroll(customer, activityClassIds);
    }

    public Enrollment findMyEnrollment(UUID authenticatedUserId) {
        Customer customer = findCustomerByAuthenticatedUser(authenticatedUserId);
        return enrollmentRepository.findByCustomer(customer)
                .orElseThrow(() -> new EnrollmentNotFoundException(
                        "No enrollment was found for the authenticated student."
                ));
    }

    public Enrollment cancelActivity(UUID authenticatedUserId, UUID enrollmentActivityId, LocalDate endDate){
        Customer customer = findCustomerByAuthenticatedUser(authenticatedUserId);
        return cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate);
    }

    private Customer findCustomerByAuthenticatedUser(UUID authenticatedUserId) {
        if (authenticatedUserId == null) {
            throw new IllegalStateException("Authenticated user could not be identified.");
        }
        return customerRepository.findByUserId(authenticatedUserId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No student profile is associated with the authenticated user."
                ));
    }
}
