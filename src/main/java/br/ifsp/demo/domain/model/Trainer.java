package br.ifsp.demo.domain.model;

import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;

public class Trainer {

    private final UUID id;
    private final String name;
    private final String email;
    private final LocalDate birthdate;
    private final Address address;
    private final boolean active;

    public Trainer(String name, String email, LocalDate birthdate, Address address) {
        this(UUID.randomUUID(), name, email, birthdate, address, true);
    }

    public Trainer(UUID id, String name) {
        this(id, name, null, null, null, true);
    }

    public Trainer(UUID id, String name, String email, LocalDate birthdate, Address address, boolean active) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.birthdate = birthdate;
        this.address = address;
        this.active = active;
    }

    public UUID getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public Address getAddress() {
        return address;
    }

    public boolean isActive() {
        return active;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Trainer trainer = (Trainer) o;
        return Objects.equals(id, trainer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}