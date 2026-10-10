package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.usecase.CreateActivityClassUseCase;
import br.ifsp.demo.domain.usecase.EditActivityClassUseCase;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Collection;
import java.util.Set;
import java.util.UUID;

@Service
public class ActivityClassService {
    private final ActivityClassRepository repository;
    private final CreateActivityClassUseCase createUseCase;
    private final EditActivityClassUseCase editUseCase;

    public ActivityClassService(ActivityClassRepository repository,
                                CreateActivityClassUseCase createUseCase,
                                EditActivityClassUseCase editUseCase) {
        this.repository = repository;
        this.createUseCase = createUseCase;
        this.editUseCase = editUseCase;
    }

    public Collection<ActivityClass> findAll() {
        return repository.findAll();
    }

    public ActivityClass findById(UUID id) {
        ActivityClass activity = repository.findById(id);
        if (activity == null) {
            throw new ResourceNotFoundException("Activity class not found: " + id);
        }
        return activity;
    }

    public ActivityClass create(UUID roomId, UUID sportId, UUID trainerId,
                                Set<DayOfWeek> weekdays, LocalTime startTime,
                                LocalTime endTime, int capacity, BigDecimal monthlyFee) {
        Room room = repository.findRoomById(roomId);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + roomId);

        Sport sport = repository.findSportById(sportId);
        if (sport == null) throw new ResourceNotFoundException("Sport not found: " + sportId);

        Trainer trainer = repository.findTrainerById(trainerId);
        if (trainer == null) throw new ResourceNotFoundException("Trainer not found: " + trainerId);

        Schedule schedule = new Schedule(weekdays, startTime, endTime);
        return createUseCase.createNewActivityClass(room, sport, trainer, schedule,
                capacity, monthlyFee);
    }

    public ActivityClass edit(UUID activityId, UUID roomId, UUID sportId, UUID trainerId,
                              Set<DayOfWeek> weekdays, LocalTime startTime,
                              LocalTime endTime, int capacity, BigDecimal monthlyFee) {
        findById(activityId);

        Room room = repository.findRoomById(roomId);
        if (room == null) throw new ResourceNotFoundException("Room not found: " + roomId);

        Sport sport = repository.findSportById(sportId);
        if (sport == null) throw new ResourceNotFoundException("Sport not found: " + sportId);

        Trainer trainer = repository.findTrainerById(trainerId);
        if (trainer == null) throw new ResourceNotFoundException("Trainer not found: " + trainerId);

        Schedule schedule = new Schedule(weekdays, startTime, endTime);
        return editUseCase.edit(activityId, room, sport, trainer, schedule,
                capacity, monthlyFee);
    }
}
