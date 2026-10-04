package br.ifsp.demo.domain.usecase;

import java.math.BigDecimal;

import br.ifsp.demo.domain.repository.EnrollmentRepository;
import org.mockito.Mock;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.assertThat;

import br.ifsp.demo.domain.model.*;


@ExtendWith(MockitoExtension.class)
public class CancelEnrollmentActivityUseCaseTest {
    @InjectMocks
    private CancelEnrollmentActivityUseCase sut;
    @Mock
    private EnrollmentRepository enrollmentRepo;
    @Mock
    private Customer customer;
    @Mock
    private Enrollment enrollment;
    @Mock
    private ActivityClass activityClass;


    @BeforeEach
    public void setup() {
        customer = new Customer("Test", "test@gmail.com");
        enrollment = new Enrollment(customer);
    }

    @Test
    @Tag("TDD")
    @Tag("UnitTest")
    @DisplayName("Should cancel the selected activity enrollment")
    void shouldCancelSelectedActivityEnrollment() {
        ActivityClass otherActivity = mock(ActivityClass.class);

        when(activityClass.getMonthlyFee()).thenReturn(new BigDecimal("100.00"));
        when(otherActivity.getMonthlyFee()).thenReturn(new BigDecimal("100.00"));

        enrollment.addActivity(activityClass);
        enrollment.addActivity(otherActivity);

        EnrollmentActivity enrollmentActivityToCancel = enrollment.getEnrollmentActivities().get(0);
        EnrollmentActivity otherEnrollmentActivity = enrollment.getEnrollmentActivities().get(1);

        when(enrollmentRepo.findByCustomer(customer)).thenReturn(enrollment);

        sut.cancelActivity(customer, enrollmentActivityToCancel.getId());

        assertThat(enrollment.getEnrollmentActivities()).doesNotContain(enrollmentActivityToCancel);
        assertThat(enrollment.getEnrollmentActivities()).contains(otherEnrollmentActivity);
        verify(enrollmentRepo).save(enrollment);
    }
}
