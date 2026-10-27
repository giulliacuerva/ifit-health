package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.repository.ActivityClassRepository;

import br.ifsp.demo.exception.RoomTypeConflictException;
import br.ifsp.demo.exception.RoomScheduleConflictException;
import br.ifsp.demo.exception.TrainerScheduleConflictException;
import br.ifsp.demo.exception.CapacityIsGreaterThanAcceptedException;

import java.math.BigDecimal;
import java.util.Objects;
import org.springframework.stereotype.Service;


@Service
public class CreateActivityClassUseCase {
    private final ActivityClassRepository activityClassRepo;

    public CreateActivityClassUseCase(ActivityClassRepository activityClassRepo) {
        this.activityClassRepo = activityClassRepo;
    }

    public ActivityClass createNewActivityClass(Room room, Sport sport, Trainer trainer, Schedule schedule, int capacity, BigDecimal monthlyFee) {
        Objects.requireNonNull(room, "Room cannot be null");
        Objects.requireNonNull(sport, "Sport cannot be null");
        Objects.requireNonNull(trainer, "Trainer cannot be null");
        Objects.requireNonNull(schedule, "Schedule cannot be null");
        Objects.requireNonNull(monthlyFee, "Monthly fee cannot be null");

        validateRoomConflict(room, schedule);
        validateTrainerConflict(trainer, schedule);
        validateRoomType(room, sport);
        validateCapacityExceeds(room, capacity);

        ActivityClass activityClass = new ActivityClass(room, sport, trainer, schedule, capacity, monthlyFee);

        return activityClassRepo.save(activityClass);
    }

    private void validateCapacityExceeds(Room room, int capacity) {
        if (capacity > room.getCapacity()) {
            throw new CapacityIsGreaterThanAcceptedException("Capacity exceeds room limit");
        }
    }

    private void validateRoomType(Room room, Sport sport) {
        if (!room.getType().equals(sport.getRoomType())) {
            throw new RoomTypeConflictException("Room type does not match sport requirements");
        }
    }

    private void validateRoomConflict(Room room, Schedule schedule) {
        boolean conflict = activityClassRepo.findByRoom(room)
                .stream()
                .anyMatch(activityClass -> activityClass.getSchedule().conflictsWith(schedule));

        if (conflict) {
            throw new RoomScheduleConflictException("Room is already booked for the given schedule");
        }
    }

    private void validateTrainerConflict(Trainer trainer, Schedule schedule) {
        boolean conflict = activityClassRepo.findByTrainer(trainer)
                .stream()
                .anyMatch(activityClass -> activityClass.getSchedule().conflictsWith(schedule));

        if (conflict) { throw new TrainerScheduleConflictException("Trainer is already booked for the given schedule"); }
    }
}
