package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.exception.RoomScheduleConflictException;
import br.ifsp.demo.exception.TrainerScheduleConflictException;

import java.math.BigDecimal;
import java.util.UUID;

public class EditActivityClassUseCase {

    private final ActivityClassRepository activityClassRepo;
    private final EnrollmentRepository enrollmentRepo;

    public EditActivityClassUseCase(
            ActivityClassRepository activityClassRepo,
            EnrollmentRepository enrollmentRepo
    ) {
        this.activityClassRepo = activityClassRepo;
        this.enrollmentRepo = enrollmentRepo;
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
        validateTrainerConflict(activityClassId, trainer, schedule);
        validateStudentScheduleConflict(activityClass, schedule);

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

    private void validateTrainerConflict(
            UUID activityClassId,
            Trainer trainer,
            Schedule schedule
    ) {
        boolean conflict = activityClassRepo.findByTrainer(trainer)
                .stream()
                .filter(activity -> !activity.getId().equals(activityClassId))
                .anyMatch(activity ->
                        activity.getSchedule().conflictsWith(schedule)
                );

        if (conflict) {
            throw new TrainerScheduleConflictException(
                    "Trainer has a schedule conflict"
            );
        }
    }

    private void validateStudentScheduleConflict(
            ActivityClass activityClass,
            Schedule newSchedule
    ) {
        boolean conflict = enrollmentRepo
                .findActivitiesByActivityClass(activityClass)
                .stream()
                .anyMatch(enrollmentActivity ->
                        enrollmentActivity
                                .getActivityClass()
                                .getSchedule()
                                .conflictsWith(newSchedule)
                );

        if (conflict) {
            throw new ActivityScheduleConflictException(
                    "Student has a schedule conflict"
            );
        }
    }
}