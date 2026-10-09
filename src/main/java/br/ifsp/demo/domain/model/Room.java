package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.util.Objects;
import java.util.UUID;

public class Room {

    private final UUID id;
    private final String name;
    private final RoomType type;
    private final int capacity;

    public Room(String name, RoomType type, int capacity) {
        this(UUID.randomUUID(), name, type, capacity);
    }

    public Room(UUID id, String name, RoomType type, int capacity) {
        this.id = id;
        this.name = name;
        this.type = type;
        this.capacity = capacity;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public RoomType getType() {
        return type;
    }

    public int getCapacity() {
        return capacity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Room room = (Room) o;

        return Objects.equals(id, room.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}