package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;

import br.ifsp.demo.domain.repository.ActivityClassRepository;


public class CreateActivityClassUseCase {
    private final ActivityClassRepository activityClassRepo;

    public CreateActivityClassUseCase(ActivityClassRepository activityClassRepo) {
        this.activityClassRepo = activityClassRepo;
    }

    public ActivityClass createNewActivityClass(Room room, Sport sport, Trainer trainer, Schedule schedule) {

        ActivityClass activityClass = new ActivityClass(room, sport, trainer, schedule);

        return activityClassRepo.save(activityClass);
    }
}
