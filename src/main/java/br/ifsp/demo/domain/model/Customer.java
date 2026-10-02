package br.ifsp.demo.domain.model;

import java.util.UUID;

public class Customer {
    private final UUID uuid;
    private final String name;
    private final String email;

    public Customer(String name, String email) {
        this.uuid = UUID.randomUUID();
        this.name = name;
        this.email = email;
    }
}
