package br.ifsp.demo.domain.model;

import java.util.UUID;

public class Room {
    private final UUID id;
    private final String name;

    public Room(UUID id, String name) {
        this.id = id;
        this.name = name;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

}
