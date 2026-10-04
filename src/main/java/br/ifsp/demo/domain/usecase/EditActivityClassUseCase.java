package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.repository.ActivityClassRepository;

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
}