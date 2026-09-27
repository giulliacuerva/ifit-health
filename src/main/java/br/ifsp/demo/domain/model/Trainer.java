package br.ifsp.demo.domain.model;

import java.util.UUID;

public class Trainer {
    private final UUID id;
    private final String name;

    public Trainer(UUID id, String name) {
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
