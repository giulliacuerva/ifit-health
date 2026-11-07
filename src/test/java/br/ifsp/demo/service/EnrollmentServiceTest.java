
package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private ActivityClassRepository activityClassRepository;

    @Mock
    private EnrollCustomerUseCase enrollUseCase;

    @Mock
    private CancelEnrollmentActivityUseCase cancelUseCase;

    @InjectMocks
    private EnrollmentService service;

    private UUID customerId;
    private UUID authenticatedUserId;
    private UUID activityClassId;
    private UUID enrollmentActivityId;

    private Customer customer;
    private ActivityClass activityClass;
    private Enrollment enrollment;

    private LocalDate endDate;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        activityClassId = UUID.randomUUID();
        enrollmentActivityId = UUID.randomUUID();

        customer = mock(Customer.class);
        activityClass = mock(ActivityClass.class);
        enrollment = mock(Enrollment.class);

        endDate = LocalDate.of(2026, 11, 6);
    }


    @Test
    void shouldEnrollCustomerWhenCustomerAndActivitiesExist() {
        List<UUID> activityClassIds = List.of(activityClassId);

        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.of(customer));

        when(activityClassRepository.findById(activityClassId))
                .thenReturn(activityClass);

        when(enrollUseCase.enroll(customer, activityClassIds))
                .thenReturn(enrollment);

        Enrollment result = service.enroll(
                customerId, authenticatedUserId, activityClassIds
        );

        assertThat(result).isEqualTo(enrollment);

        verify(enrollmentRepository).findCustomerById(customerId);
        verify(activityClassRepository).findById(activityClassId);
        verify(enrollUseCase).enroll(customer, activityClassIds);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringEnrollment() {
        List<UUID> activityClassIds = List.of(activityClassId);

        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.enroll(customerId, authenticatedUserId, activityClassIds)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verifyNoInteractions(activityClassRepository);
        verifyNoInteractions(enrollUseCase);
    }

    @Test
    void shouldThrowExceptionWhenActivityClassNotFoundDuringEnrollment() {
        List<UUID> activityClassIds = List.of(activityClassId);

        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.of(customer));

        when(activityClassRepository.findById(activityClassId))
                .thenReturn(null);

        assertThatThrownBy(() -> service.enroll(customerId, authenticatedUserId, activityClassIds)).isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Activity class not found");

        verify(enrollmentRepository).findCustomerById(customerId);
        verify(activityClassRepository).findById(activityClassId);
        verifyNoInteractions(enrollUseCase);
    }

    @Test
    void shouldValidateAllActivityClassesBeforeEnrollment() {
        UUID secondActivityClassId = UUID.randomUUID();

        List<UUID> activityClassIds = List.of(activityClassId, secondActivityClassId);

        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.of(customer));

        when(activityClassRepository.findById(activityClassId))
                .thenReturn(activityClass);

        when(activityClassRepository.findById(secondActivityClassId))
                .thenReturn(activityClass);

        when(enrollUseCase.enroll(customer, activityClassIds))
                .thenReturn(enrollment);

        Enrollment result = service.enroll(
                customerId, authenticatedUserId, activityClassIds
        );

        assertThat(result).isEqualTo(enrollment);

        verify(activityClassRepository).findById(activityClassId);
        verify(activityClassRepository).findById(secondActivityClassId);
        verify(enrollUseCase).enroll(customer, activityClassIds);
    }


    @Test
    void shouldCancelEnrollmentActivityWhenCustomerExists() {
        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.of(customer));

        when(cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate)).thenReturn(enrollment);

        Enrollment result = service.cancelActivity(customerId, authenticatedUserId, enrollmentActivityId, endDate);

        assertThat(result).isEqualTo(enrollment);

        verify(enrollmentRepository).findCustomerById(customerId);

        verify(cancelUseCase).cancelActivity(customer, enrollmentActivityId, endDate);
    }

    @Test
    void shouldThrowExceptionWhenCustomerNotFoundDuringCancellation() {
        when(enrollmentRepository.findCustomerById(customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.cancelActivity(customerId, authenticatedUserId, enrollmentActivityId, endDate))
                .isInstanceOf(ResourceNotFoundException.class).hasMessageContaining("Customer not found");

        verifyNoInteractions(cancelUseCase);
    }
}
