package br.ifsp.demo.domain.usecase;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class EnrollCustomerUseCaseTest {

    @InjectMocks
    private EnrollCustomerUseCase sut;
    @Mock
    private ActivityClassRepository activityClassRepo;
    @Mock
    private EnrollmentRepository enrollmentRepo;

    private ActivityClass activityClass;

    @BeforeEach
    public void setup() {
        Trainer trainer = new Trainer(UUID.randomUUID(), "John Doe");
        Room room = new Room(UUID.randomUUID(), "Room A", RoomType.GYM, 10);
        Sport sport = new Sport(UUID.randomUUID(), "Basketball", RoomType.GYM);
        Set<DayOfWeek> classDays = Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY);
        Schedule schedule = new Schedule(classDays, LocalTime.of(10, 0),LocalTime.of(11, 0));
        BigDecimal monthlyFee = new BigDecimal("250.00");

        this.activityClass = new ActivityClass(room, sport, trainer, schedule, 10, monthlyFee);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should enroll customer in activity")
    void shouldEnrollCustomerInActivity() {
        UUID activityClassId = UUID.randomUUID();
        Customer customer = new Customer("teste", "teste@gmail.com");
        Enrollment enrollment = new Enrollment(customer);

        List<EnrollmentActivity> enrollmentActivities = List.of();

        when(activityClassRepo.findById(activityClassId)).thenReturn(activityClass);
        when(enrollmentRepo.findActivitiesByActivityClass(activityClass)).thenReturn(enrollmentActivities);
        when(enrollmentRepo.findByCustomer(customer)).thenReturn(enrollment);
        when(enrollmentRepo.save(enrollment)).thenReturn(enrollment);

        Enrollment result = sut.enroll(customer, activityClassId);

        assertThat(result).isEqualTo(enrollment);
        assertThat(result.getCustomer()).isEqualTo(customer);
        assertThat(result.getEnrollmentActivities()).hasSize(1);

        verify(enrollmentRepo).save(enrollment);
    }

}
