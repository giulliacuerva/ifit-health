package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.EnrollmentRequest;
import br.ifsp.demo.controller.dto.EnrollmentResponse;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import br.ifsp.demo.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/enrollment")
@Tag(name = "Student enrollment")
public class EnrollmentController {
    private final EnrollmentService enrollmentService;
    private final AuthenticationInfoService authenticationInfo;

    public EnrollmentController(EnrollmentService enrollmentService,
                                AuthenticationInfoService authenticationInfo) {
        this.enrollmentService = enrollmentService;
        this.authenticationInfo = authenticationInfo;
    }

    @GetMapping
    @Operation(summary = "Get the authenticated student's enrollment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Enrollment returned"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Student or enrollment not found")
    })
    public EnrollmentResponse getMyEnrollment() {
        return EnrollmentResponse.from(
                enrollmentService.findMyEnrollment(authenticationInfo.getAuthenticatedUserId())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Enroll the authenticated student in activities")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enrollment created or updated"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Student or activity not found"),
            @ApiResponse(responseCode = "409", description = "No vacancies or enrollment conflict")
    })
    public EnrollmentResponse enroll(@Valid @RequestBody EnrollmentRequest request) {
        return EnrollmentResponse.from(
                enrollmentService.enroll(
                        authenticationInfo.getAuthenticatedUserId(),
                        request.activityClassIds()
                )
        );
    }

    @DeleteMapping("/activities/{enrollmentActivityId}")
    @Operation(summary = "Cancel one activity from the authenticated student's enrollment")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity canceled"),
            @ApiResponse(responseCode = "401", description = "Authentication required"),
            @ApiResponse(responseCode = "404", description = "Enrollment or activity not found"),
            @ApiResponse(responseCode = "409", description = "Activity is already inactive or date is invalid")
    })
    public EnrollmentResponse cancel(
            @PathVariable UUID enrollmentActivityId,
            @RequestParam(required = false) LocalDate endDate) {

        LocalDate effectiveEndDate = endDate == null ? LocalDate.now() : endDate;

        return EnrollmentResponse.from(
                enrollmentService.cancelActivity(
                        authenticationInfo.getAuthenticatedUserId(),
                        enrollmentActivityId,
                        effectiveEndDate
                )
        );
    }
}
