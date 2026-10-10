package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.EnrollmentRequest;
import br.ifsp.demo.controller.dto.EnrollmentResponse;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/enrollment")
public class EnrollmentController {
    private final EnrollmentRepository repository;
    private final ActivityClassRepository activityClassRepository;
    private final EnrollCustomerUseCase enroll;
    private final CancelEnrollmentActivityUseCase cancel;
    private final AuthenticationInfoService authenticationInfo;

    public EnrollmentController(EnrollmentRepository repository,
                                ActivityClassRepository activityClassRepository,
                                EnrollCustomerUseCase enroll,
                                CancelEnrollmentActivityUseCase cancel,
                                AuthenticationInfoService authenticationInfo) {
        this.repository = repository;
        this.activityClassRepository = activityClassRepository;
        this.enroll = enroll;
        this.cancel = cancel;
        this.authenticationInfo = authenticationInfo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@PathVariable UUID customerId,
                                     @Valid @RequestBody EnrollmentRequest request) {
        var customer = repository.findCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        assertCustomerOwnsRequest(customer.getUserId());
        for (UUID activityClassId : request.activityClassIds()) {
            if (activityClassRepository.findById(activityClassId) == null) {
                throw new ResourceNotFoundException("Activity class not found: " + activityClassId);
            }
        }
        return EnrollmentResponse.from(enroll.enroll(customer, request.activityClassIds()));
    }

    @DeleteMapping("/activities/{enrollmentActivityId}")
    public EnrollmentResponse cancel(@PathVariable UUID customerId,
                                     @PathVariable UUID enrollmentActivityId,
                                     @RequestParam(required = false) java.time.LocalDate endDate) {
        var customer = repository.findCustomerById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Customer not found: " + customerId));
        assertCustomerOwnsRequest(customer.getUserId());
        var effectiveEndDate = endDate == null ? java.time.LocalDate.now() : endDate;
        return EnrollmentResponse.from(cancel.cancelActivity(customer, enrollmentActivityId,
                effectiveEndDate));
    }

    private void assertCustomerOwnsRequest(UUID linkedUserId) {
        if (linkedUserId == null || !linkedUserId.equals(authenticationInfo.getAuthenticatedUserId())) {
            throw new AccessDeniedException("The authenticated user does not own this customer");
        }
    }
}
