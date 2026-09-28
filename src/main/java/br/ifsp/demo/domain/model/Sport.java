package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.util.Objects;
import java.util.UUID;

public class Sport {
    private final UUID id;
    private final String name;
    private final RoomType roomType;

    public Sport(UUID id, String name, RoomType roomType) {
        this.id = id;
        this.name = name;
        this.roomType = roomType;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Sport sport = (Sport) o;
        return Objects.equals(id, sport.id) && Objects.equals(name, sport.name) && roomType == sport.roomType;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, roomType);
    }
}
