package br.ifsp.demo.controller;

import br.ifsp.demo.controller.dto.ActivityClassRequest;
import br.ifsp.demo.controller.dto.ActivityClassResponse;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.CreateActivityClassUseCase;
import br.ifsp.demo.domain.usecase.EditActivityClassUseCase;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.Collection;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/activity-classes")
public class ActivityClassController {
    private final ActivityClassRepository repository;
    private final CreateActivityClassUseCase create;
    private final EditActivityClassUseCase edit;

    public ActivityClassController(ActivityClassRepository repository,
                                   CreateActivityClassUseCase create,
                                   EditActivityClassUseCase edit) {
        this.repository = repository;
        this.create = create;
        this.edit = edit;
    }

    @GetMapping
    public Collection<ActivityClassResponse> findAll() {
        return repository.findAll().stream().map(ActivityClassResponse::from).toList();
    }

    @GetMapping("/{id}")
    public ActivityClassResponse findById(@PathVariable UUID id) {
        var activity = repository.findById(id);
        if (activity == null) throw new ResourceNotFoundException("Activity class not found: " + id);
        return ActivityClassResponse.from(activity);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ActivityClassResponse create(@Valid @RequestBody ActivityClassRequest request) {
        var room = repository.findRoomById(request.roomId());
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + request.roomId());
        }

        var sport = repository.findSportById(request.sportId());
        if (sport == null) {
            throw new ResourceNotFoundException("Sport not found: " + request.sportId());
        }

        var trainer = repository.findTrainerById(request.trainerId());
        if (trainer == null) {
            throw new ResourceNotFoundException("Trainer not found: " + request.trainerId());
        }

        var schedule = new Schedule(request.weekdays(), request.startTime(), request.endTime());
        return ActivityClassResponse.from(create.createNewActivityClass(room, sport, trainer,
                schedule, request.capacity(), request.monthlyFee()));
    }

    @PutMapping("/{id}")
    public ActivityClassResponse edit(@PathVariable UUID id,
                                      @Valid @RequestBody ActivityClassRequest request) {
        if (repository.findById(id) == null) {
            throw new ResourceNotFoundException("Activity class not found: " + id);
        }
        var room = repository.findRoomById(request.roomId());
        if (room == null) {
            throw new ResourceNotFoundException("Room not found: " + request.roomId());
        }

        var sport = repository.findSportById(request.sportId());
        if (sport == null) {
            throw new ResourceNotFoundException("Sport not found: " + request.sportId());
        }

        var trainer = repository.findTrainerById(request.trainerId());
        if (trainer == null) {
            throw new ResourceNotFoundException("Trainer not found: " + request.trainerId());
        }

        var schedule = new Schedule(request.weekdays(), request.startTime(), request.endTime());
        return ActivityClassResponse.from(edit.edit(id, room, sport, trainer, schedule,
                request.capacity(), request.monthlyFee()));
    }

}
