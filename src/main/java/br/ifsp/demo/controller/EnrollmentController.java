package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.EnrollmentRequest;
import br.ifsp.demo.controller.dto.EnrollmentResponse;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import br.ifsp.demo.service.EnrollmentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/customers/{customerId}/enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final AuthenticationInfoService authenticationInfo;

    public EnrollmentController(EnrollmentService enrollmentService,
                                AuthenticationInfoService authenticationInfo) {
        this.enrollmentService = enrollmentService;
        this.authenticationInfo = authenticationInfo;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentResponse enroll(@PathVariable UUID customerId,
                                     @Valid @RequestBody EnrollmentRequest request) {
        return EnrollmentResponse.from(enrollmentService.enroll(customerId,
                authenticationInfo.getAuthenticatedUserId(), request.activityClassIds()));
    }

    @DeleteMapping("/activities/{enrollmentActivityId}")
    public EnrollmentResponse cancel(@PathVariable UUID customerId,
                                     @PathVariable UUID enrollmentActivityId,
                                     @RequestParam(required = false) java.time.LocalDate endDate) {
        var effectiveEndDate = endDate == null ? java.time.LocalDate.now() : endDate;
        return EnrollmentResponse.from(enrollmentService.cancelActivity(customerId,
                authenticationInfo.getAuthenticatedUserId(), enrollmentActivityId,
                effectiveEndDate));
    }
}
