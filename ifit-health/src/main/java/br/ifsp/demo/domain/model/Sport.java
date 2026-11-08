package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.RoomType;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Sport {

    private final UUID id;
    private final String name;
    private final String description;
    private final RoomType roomType;
    private final boolean active;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Sport(UUID id, String name, RoomType roomType) {
        this(id, name, null, roomType, true, LocalDateTime.now(), LocalDateTime.now());
    }

    public Sport(UUID id, String name, String description, RoomType roomType, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.roomType = roomType;
        this.active = active;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public RoomType getRoomType() {
        return roomType;
    }

    public boolean isActive() {
        return active;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Sport sport = (Sport) o;

        return Objects.equals(id, sport.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}