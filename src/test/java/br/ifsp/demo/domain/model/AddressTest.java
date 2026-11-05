package br.ifsp.demo.domain.model;



import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AddressTest {

    private Address createAddress(String cep, String number, String street, String city, String state) {
        return new Address(cep, number, street, city, state);
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should consider the same address equal")
    void shouldConsiderTheSameAddressEqual() {
        Address address = createAddress("12345-678", "10", "Rua A", "São Carlos", "SP");

        assertThat(address.equals(address)).isTrue();
    }

    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when compared with null")
    void shouldReturnFalseWhenComparedWithNull() {
        Address address = createAddress("12345-678", "10", "Rua A", "São Carlos", "SP");

        assertThat(address.equals(null)).isFalse();
    }

}
