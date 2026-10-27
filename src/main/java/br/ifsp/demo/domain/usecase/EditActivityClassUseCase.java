package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.exception.ActivityScheduleConflictException;
import br.ifsp.demo.exception.CapacityIsGreaterThanAcceptedException;
import br.ifsp.demo.exception.RoomScheduleConflictException;
import br.ifsp.demo.exception.RoomTypeConflictException;
import br.ifsp.demo.exception.TrainerScheduleConflictException;

import java.math.BigDecimal;
import java.util.Objects;
import java.util.UUID;
import org.springframework.stereotype.Service;

@Service
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

        Objects.requireNonNull(room, "Room cannot be null");
        Objects.requireNonNull(sport, "Sport cannot be null");
        Objects.requireNonNull(trainer, "Trainer cannot be null");
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(monthlyFee, "Monthly fee cannot be null");

        validateRoomType(room, sport);
        validateCapacityExceedsRoom(room, capacity);
        validatePositiveCapacity(capacity);

        validateRoomConflict(activityClassId, room, schedule);
        validateTrainerConflict(activityClassId, trainer, schedule);
        validateStudentScheduleConflict(activityClass, schedule);
        validateCapacity(activityClass, capacity);

        activityClass.edit(room, sport, trainer, schedule,capacity,monthlyFee);

        return activityClassRepo.save(activityClass);
    }

    private void validateRoomType(Room room, Sport sport) {
        if (!room.getType().equals(sport.getRoomType())) {
            throw new RoomTypeConflictException(
                    "Room type does not match sport requirements"
            );
        }
    }

    private void validateCapacityExceedsRoom(Room room, int capacity) {
        if (capacity > room.getCapacity()) {
            throw new CapacityIsGreaterThanAcceptedException(
                    "Capacity exceeds room limit"
            );
        }
    }

    private void validatePositiveCapacity(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException(
                    "Capacity must be greater than zero"
            );
        }
    }

    private void validateRoomConflict(UUID activityClassId,Room room,Schedule schedule) {
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

    private void validateTrainerConflict(UUID activityClassId, Trainer trainer, Schedule schedule) {
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

    private void validateStudentScheduleConflict(ActivityClass activityClass,Schedule newSchedule) {
        boolean conflict = enrollmentRepo
                .findEnrollmentsByActivityClass(activityClass)
                .stream()
                .flatMap(enrollment ->
                        enrollment.getEnrollmentActivities().stream())
                .filter(EnrollmentActivity::isActive)
                .filter(activity ->
                        !activity.getActivityClass()
                                .getId()
                                .equals(activityClass.getId()))
                .anyMatch(activity ->
                        activity.getActivityClass()
                                .getSchedule()
                                .conflictsWith(newSchedule)
                );

        if (conflict) {
            throw new ActivityScheduleConflictException(
                    "Student has a schedule conflict"
            );
        }
    }

    private void validateCapacity(ActivityClass activityClass, int newCapacity) {
        int enrolledStudents = enrollmentRepo
                .findEnrolledActivitiesByActivityClass(activityClass)
                .size();

        if (newCapacity < enrolledStudents) {
            throw new IllegalStateException(
                    "New capacity cannot be lower than enrolled students: "
                            + enrolledStudents
            );
        }
    }
}
