package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.exception.RoomScheduleConflictException;

import java.math.BigDecimal;
import java.util.UUID;

public class EditActivityClassUseCase {

    private final ActivityClassRepository activityClassRepo;

    public EditActivityClassUseCase(ActivityClassRepository activityClassRepo) {
        this.activityClassRepo = activityClassRepo;
    }

    public ActivityClass edit(
            UUID activityClassId,
            Room room,
            Sport sport,
            Trainer trainer,
            Schedule schedule,
            int capacity,
            BigDecimal monthlyFee
    ) {
        ActivityClass activityClass =
                activityClassRepo.findById(activityClassId);

        validateRoomConflict(activityClassId, room, schedule);

        activityClass.edit(
                room,
                sport,
                trainer,
                schedule,
                capacity,
                monthlyFee
        );

        return activityClassRepo.save(activityClass);
    }

    private void validateRoomConflict(
            UUID activityClassId,
            Room room,
            Schedule schedule
    ) {
        boolean conflict = activityClassRepo.findByRoom(room)
                .stream()
                .filter(activity -> !activity.getId().equals(activityClassId))
                .anyMatch(activity ->
                        activity.getSchedule().conflictsWith(schedule)
                );

        if (conflict) {
            throw new RoomScheduleConflictException(
                    "Room has a schedule conflict"
            );
        }
    }
}