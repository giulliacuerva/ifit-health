package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.EnrollmentRequest;
import br.ifsp.demo.controller.dto.EnrollmentResponse;
import br.ifsp.demo.exception.ApiException;
import br.ifsp.demo.security.auth.AuthenticationInfoService;
import br.ifsp.demo.service.EnrollmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/me/enrollment")
@Tag(name = "Student enrollment", description = "Enrollment operations for the authenticated student.")
@SecurityRequirement(name = "bearerAuth")
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
            @ApiResponse(responseCode = "200", description = "Enrollment returned.",
                    content = @Content(schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "The endpoint is restricted to the USER profile."),
            @ApiResponse(responseCode = "404", description = "No student profile or enrollment was found.",
                    content = @Content(schema = @Schema(implementation = ApiException.class)))
    })
    public EnrollmentResponse getMyEnrollment() {
        return EnrollmentResponse.from(
                enrollmentService.findMyEnrollment(authenticationInfo.getAuthenticatedUserId())
        );
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Enroll the authenticated student in activities",
            description = "Creates or updates the authenticated student's enrollment for the selected activity classes."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Enrollment created or updated.",
                    content = @Content(schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "400", description = "The activityClassIds list is empty or invalid."),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "The endpoint is restricted to the USER profile."),
            @ApiResponse(responseCode = "404", description = "Student profile or activity class was not found.",
                    content = @Content(schema = @Schema(implementation = ApiException.class))),
            @ApiResponse(responseCode = "409", description = "No vacancies or an enrollment conflict occurred.",
                    content = @Content(schema = @Schema(implementation = ApiException.class)))
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
    @Operation(
            summary = "Cancel one activity from the authenticated student's enrollment",
            description = "Cancels only the selected enrollment activity. If endDate is omitted, the current date is used."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity canceled.",
                    content = @Content(schema = @Schema(implementation = EnrollmentResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "The endpoint is restricted to the USER profile."),
            @ApiResponse(responseCode = "404", description = "Enrollment or activity was not found.",
                    content = @Content(schema = @Schema(implementation = ApiException.class))),
            @ApiResponse(responseCode = "409", description = "Activity is already inactive or the cancellation date is invalid.",
                    content = @Content(schema = @Schema(implementation = ApiException.class)))
    })
    public EnrollmentResponse cancel(
            @Parameter(description = "Enrollment activity UUID.", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440001")
            @PathVariable UUID enrollmentActivityId,
            @Parameter(description = "Optional cancellation date (yyyy-MM-dd). Defaults to the current date.",
                    example = "2026-11-07")
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
