package br.ifsp.demo.domain.model;

import java.util.Objects;

public class Address {

    private final String cep;
    private final String number;
    private final String street;
    private final String city;
    private final String state;

    public Address(String cep, String number, String street, String city, String state) {
        this.cep = Objects.requireNonNull(cep);
        this.number = Objects.requireNonNull(number);
        this.street = Objects.requireNonNull(street);
        this.city = Objects.requireNonNull(city);
        this.state = Objects.requireNonNull(state);
    }

    public String getCep() {
        return cep;
    }

    public String getNumber() {
        return number;
    }

    public String getStreet() {
        return street;
    }

    public String getCity() {
        return city;
    }

    public String getState() {
        return state;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Address address = (Address) o;

        return Objects.equals(cep, address.cep)
                && Objects.equals(number, address.number)
                && Objects.equals(street, address.street)
                && Objects.equals(city, address.city)
                && Objects.equals(state, address.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(cep, number, street, city, state);
    }
}