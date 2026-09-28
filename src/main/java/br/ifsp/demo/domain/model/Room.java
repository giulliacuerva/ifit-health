package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.util.Objects;
import java.util.UUID;

public class Room {
    private final UUID id;
    private final String name;
    private final RoomType type;

    public Room(UUID id, String name, RoomType type) {
        this.id = id;
        this.name = name;
        this.type = type;
    }

    public RoomType getType() {
        return type;
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
        Room room = (Room) o;
        return Objects.equals(id, room.id) && Objects.equals(name, room.name) && type == room.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, type);
    }
}
