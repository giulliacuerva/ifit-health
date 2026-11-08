package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityClassAvailabilityResponse;
import br.ifsp.demo.controller.dto.ActivityClassRequest;
import br.ifsp.demo.controller.dto.ActivityClassResponse;
import br.ifsp.demo.controller.dto.ActivityScheduleConflictResponse;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.service.ActivityClassQueryService;
import br.ifsp.demo.service.ActivityClassService;
import br.ifsp.demo.service.ScheduleConflictQueryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-classes")
@Tag(name = "Activity classes", description = "search, create and edit classes")
@SecurityRequirement(name = "bearerAuth")
public class ActivityClassController {
    private final ActivityClassService activityClassService;
    private final ActivityClassQueryService activityClassQueryService;
    private final ScheduleConflictQueryService scheduleConflictQueryService;

    public ActivityClassController(
            ActivityClassService activityClassService,
            ActivityClassQueryService activityClassQueryService,
            ScheduleConflictQueryService scheduleConflictQueryService
    ) {
        this.activityClassService = activityClassService;
        this.activityClassQueryService = activityClassQueryService;
        this.scheduleConflictQueryService = scheduleConflictQueryService;
    }

    @GetMapping
    @Operation(
            summary = "List activity classes",
            description = "Returns activity classes with catalog names, enrollment count and available spots. "
                    + "The active query parameter is optional."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity classes returned.",
                    content = @Content(array = @ArraySchema(
                            schema = @Schema(implementation = ActivityClassAvailabilityResponse.class)))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "The authenticated profile is not allowed to list activities.")
    })
    public List<ActivityClassAvailabilityResponse> findAll(
            @Parameter(description = "When true, returns only active activities; when false, only inactive ones.",
                    example = "true")
            @RequestParam(value = "active", required = false) Boolean active,
            Authentication authentication
    ) {

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ADMIN")
                );

        return activityClassQueryService.findAll(active, isAdmin);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get an activity class by ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity class returned.",
                    content = @Content(schema = @Schema(implementation = ActivityClassResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "The authenticated profile is not allowed to access activities."),
            @ApiResponse(responseCode = "404", description = "Activity class not found.")
    })
    public ActivityClassResponse findById(
            @Parameter(name = "id", description = "Activity class UUID.", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            Authentication authentication
    ) {

        var activity = activityClassService.findById(id);

        boolean isAdmin = authentication.getAuthorities()
                .stream()
                .anyMatch(authority ->
                        authority.getAuthority().equals("ADMIN")
                );

        if (isAdmin) {
            var latestPrice = activityClassQueryService.findLatestPrice(id, activity.getMonthlyFee());
            return ActivityClassResponse.from(activity, latestPrice);
        }
        return ActivityClassResponse.from(activity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(
            summary = "Create an activity class",
            description = "Creates a class after validating the selected sport, room, trainer, schedule and capacity. "
                    + "This operation is restricted to administrators."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Activity class created.",
                    content = @Content(schema = @Schema(implementation = ActivityClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "The request contains invalid fields or schedule data."),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can create activities."),
            @ApiResponse(responseCode = "404", description = "A referenced sport, room or trainer was not found."),
            @ApiResponse(responseCode = "409", description = "A room/trainer schedule conflict or room-type conflict occurred.",
                    content = @Content(schema = @Schema(implementation = br.ifsp.demo.exception.ApiException.class)))
    })
    public ActivityClassResponse create(@Valid @RequestBody ActivityClassRequest request) {
        return ActivityClassResponse.from(activityClassService.create(
                request.roomId(), request.sportId(), request.trainerId(),
                request.weekdays(), request.startTime(), request.endTime(),
                request.capacity(), request.monthlyFee()));
    }

    @PutMapping("/{id}")
    @Operation(
            summary = "Update an activity class",
            description = "Updates the activity class. If the new schedule conflicts with activities attended by students, "
                    + "the request is rejected and the response identifies the affected students."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Activity class updated.",
                    content = @Content(schema = @Schema(implementation = ActivityClassResponse.class))),
            @ApiResponse(responseCode = "400", description = "The request contains invalid fields or schedule data."),
            @ApiResponse(responseCode = "401", description = "Authentication is required."),
            @ApiResponse(responseCode = "403", description = "Only administrators can update activities."),
            @ApiResponse(responseCode = "404", description = "Activity class or referenced catalog item was not found."),
            @ApiResponse(responseCode = "409", description = "The update conflicts with an existing schedule.",
                    content = @Content(schema = @Schema(implementation = ActivityScheduleConflictResponse.class)))
    })
    public ResponseEntity<?> edit(
            @Parameter(name = "id", description = "Activity class UUID.", required = true,
                    example = "550e8400-e29b-41d4-a716-446655440000")
            @PathVariable UUID id,
            @Valid @RequestBody ActivityClassRequest request
    ) {
        try {
            var updatedActivity = activityClassService.edit(
                    id,
                    request.roomId(),
                    request.sportId(),
                    request.trainerId(),
                    request.weekdays(),
                    request.startTime(),
                    request.endTime(),
                    request.capacity(),
                    request.monthlyFee()
            );

            return ResponseEntity.ok(
                    ActivityClassResponse.from(updatedActivity, request.monthlyFee())
            );
        } catch (ActivityScheduleConflictException exception) {
            var affectedStudents = scheduleConflictQueryService.findAffectedStudents(
                    id,
                    request.weekdays(),
                    request.startTime(),
                    request.endTime()
            );

            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ActivityScheduleConflictResponse.from(exception, affectedStudents));
        }
    }
}
