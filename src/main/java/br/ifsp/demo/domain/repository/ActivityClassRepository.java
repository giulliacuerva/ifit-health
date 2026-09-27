package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Trainer;

import java.util.Collection;


public interface ActivityClassRepository {

    ActivityClass save(ActivityClass activityClass);

    Collection<ActivityClass> findByRoom(Room room);

    Collection<ActivityClass> findByTrainer(Trainer trainer);
}
