package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityClassAvailabilityResponse;
import br.ifsp.demo.controller.dto.ActivityClassRequest;
import br.ifsp.demo.controller.dto.ActivityClassResponse;
import br.ifsp.demo.controller.dto.ActivityScheduleConflictResponse;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.service.ActivityClassQueryService;
import br.ifsp.demo.service.ActivityClassService;
import br.ifsp.demo.service.ScheduleConflictQueryService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-classes")
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
    public List<ActivityClassAvailabilityResponse> findAll(
            @RequestParam(value = "active", required = false) Boolean active
    ) {
        return activityClassQueryService.findAll(active);
    }

    @GetMapping("/{id}")
    public ActivityClassResponse findById(@PathVariable UUID id) {
        var activity = activityClassService.findById(id);
        return ActivityClassResponse.from(activity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityClassResponse create(@Valid @RequestBody ActivityClassRequest request) {
        return ActivityClassResponse.from(activityClassService.create(
                request.roomId(), request.sportId(), request.trainerId(),
                request.weekdays(), request.startTime(), request.endTime(),
                request.capacity(), request.monthlyFee()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> edit(
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

            return ResponseEntity.ok(ActivityClassResponse.from(updatedActivity));
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
