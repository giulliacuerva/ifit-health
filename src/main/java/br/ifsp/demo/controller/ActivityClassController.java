package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityClassRequest;
import br.ifsp.demo.controller.dto.ActivityClassResponse;
import br.ifsp.demo.service.ActivityClassService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-classes")
public class ActivityClassController {
    private final ActivityClassService activityClassService;

    public ActivityClassController(ActivityClassService activityClassService) {
        this.activityClassService = activityClassService;
    }

    @GetMapping
    public List<ActivityClassResponse> findAll() {
        return activityClassService.findAll().stream()
                .map(ActivityClassResponse::from)
                .toList();
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
    public ActivityClassResponse edit(@PathVariable UUID id,
                                      @Valid @RequestBody ActivityClassRequest request) {
        return ActivityClassResponse.from(activityClassService.edit(id,
                request.roomId(), request.sportId(), request.trainerId(),
                request.weekdays(), request.startTime(), request.endTime(),
                request.capacity(), request.monthlyFee()));
    }

}
