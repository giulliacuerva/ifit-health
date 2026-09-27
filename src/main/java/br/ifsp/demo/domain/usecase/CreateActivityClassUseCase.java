package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;

import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.exception.RoomScheduleConflictException;


public class CreateActivityClassUseCase {
    private final ActivityClassRepository activityClassRepo;

    public CreateActivityClassUseCase(ActivityClassRepository activityClassRepo) {
        this.activityClassRepo = activityClassRepo;
    }

    public ActivityClass createNewActivityClass(Room room, Sport sport, Trainer trainer, Schedule schedule) {
        validateRoomConflict(room, schedule);
        ActivityClass activityClass = new ActivityClass(room, sport, trainer, schedule);

        return activityClassRepo.save(activityClass);
    }

    private void validateRoomConflict(Room room, Schedule schedule) {
        boolean conflict = activityClassRepo.findByRoom(room)
                .stream()
                .anyMatch(activityClass -> activityClass.getSchedule().conflictsWith(schedule));

        if (conflict) {
            throw new RoomScheduleConflictException("Room is already booked for the given schedule");
        }
    }
}
