package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.Sport;

import java.util.Collection;
import java.util.UUID;


public interface ActivityClassRepository {
    ActivityClass save(ActivityClass activityClass);
    ActivityClass findById(UUID activityClassId);
    Collection<ActivityClass> findByRoom(Room room);
    Collection<ActivityClass> findByTrainer(Trainer trainer);
    Collection<ActivityClass> findAll();
    Room findRoomById(UUID id);
    Sport findSportById(UUID id);
    Trainer findTrainerById(UUID id);
}
