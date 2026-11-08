package br.ifsp.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class CustomerTest {

    private Customer createCustomer(UUID id, String name) {
        LocalDateTime date = LocalDateTime.of(2026, 1, 1, 0, 0);

        return new Customer(
                id, null, null, name, null, null,
                null, null, null, date, date
        );
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should compare customers by id")
    void shouldCompareCustomersById() {
        UUID id = UUID.randomUUID();
        Customer customer = createCustomer(id, "Ana");

        assertThat(customer.equals(null)).isFalse();
        assertThat(customer.equals("not a customer")).isFalse();
        assertThat(customer.equals(createCustomer(id, "Maria"))).isTrue();
        assertThat(customer.equals(createCustomer(UUID.randomUUID(), "Ana"))).isFalse();
    }
}