package br.ifsp.demo.domain.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

public class TrainerTest {
    @Test
    @Tag("Structural")
    @Tag("UnitTest")
    @DisplayName("Should return false when compared with null")
    void shouldReturnFalseWhenComparedWithNull() {
        Trainer trainer = new Trainer(UUID.randomUUID(), "Ana");

        assertThat(trainer.equals(null)).isFalse();
    }


}
