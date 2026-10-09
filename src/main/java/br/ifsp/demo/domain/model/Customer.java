package br.ifsp.demo.domain.model;

import br.ifsp.demo.domain.model.enums.Gender;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

public class Customer {

    private final UUID id;
    private final UUID userId;
    private final String cpf;
    private final String name;
    private final LocalDate birthdate;
    private final Gender gender;
    private final String tel;
    private final String email;
    private final Address address;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public Customer(String name, String email) {
        this(UUID.randomUUID(), null, null, name, null, null, null, email, null, LocalDateTime.now(), LocalDateTime.now());
    }

    public Customer(UUID userId, String cpf, String name, LocalDate birthdate, Gender gender, String tel, String email, Address address) {
        this(UUID.randomUUID(), userId, cpf, name, birthdate, gender, tel, email, address, LocalDateTime.now(), LocalDateTime.now());
    }

    public Customer(UUID id, UUID userId, String cpf, String name, LocalDate birthdate, Gender gender, String tel, String email, Address address, LocalDateTime createdAt, LocalDateTime updatedAt) {
        this.id = id;
        this.userId = userId;
        this.cpf = cpf;
        this.name = name;
        this.birthdate = birthdate;
        this.gender = gender;
        this.tel = tel;
        this.email = email;
        this.address = address;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getId() {
        return id;
    }

    public UUID getUserId() {
        return userId;
    }

    public String getCpf() {
        return cpf;
    }

    public String getName() {
        return name;
    }

    public LocalDate getBirthdate() {
        return birthdate;
    }

    public Gender getGender() {
        return gender;
    }

    public String getTel() {
        return tel;
    }

    public String getEmail() {
        return email;
    }

    public Address getAddress() {
        return address;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Customer customer = (Customer) o;
        return Objects.equals(id, customer.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }
}