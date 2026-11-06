package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import br.ifsp.demo.domain.repository.CustomerRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.exception.ResourceNotFoundException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class EnrollmentServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ActivityClassRepository activityClassRepository;

    @Mock
    private EnrollCustomerUseCase enrollUseCase;

    @Mock
    private CancelEnrollmentActivityUseCase cancelUseCase;

    @InjectMocks
    private EnrollmentService sut;

    private UUID customerId;
    private UUID authenticatedUserId;

    private Customer customer;

    @BeforeEach
    void setUp() {
        customerId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        customer = createCustomer(customerId, authenticatedUserId);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldEnrollWhenAuthenticatedUserOwnsCustomerAndActivitiesExist() {
        UUID firstActivityId = UUID.randomUUID();
        UUID secondActivityId = UUID.randomUUID();

        ActivityClass firstActivity = createActivityClass(firstActivityId);
        ActivityClass secondActivity = createActivityClass(secondActivityId);

        List<UUID> activityClassIds = List.of(firstActivityId, secondActivityId);

        Enrollment expectedEnrollment = new Enrollment(customer);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(activityClassRepository.findById(firstActivityId))
                .thenReturn(firstActivity);

        when(activityClassRepository.findById(secondActivityId))
                .thenReturn(secondActivity);

        when(enrollUseCase.enroll(customer, activityClassIds))
                .thenReturn(expectedEnrollment);

        Enrollment result = sut.enroll(customerId, authenticatedUserId, activityClassIds);

        assertThat(result).isSameAs(expectedEnrollment);

        verify(activityClassRepository).findById(firstActivityId);
        verify(activityClassRepository).findById(secondActivityId);
        verify(enrollUseCase).enroll(customer, activityClassIds);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldRejectEnrollmentWhenActivityClassDoesNotExist() {
        UUID activityClassId = UUID.randomUUID();
        List<UUID> activityClassIds = List.of(activityClassId);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(activityClassRepository.findById(activityClassId))
                .thenReturn(null);

        assertThatThrownBy(() -> sut.enroll(customerId, authenticatedUserId, activityClassIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Activity class not found: " + activityClassId);

        verify(activityClassRepository).findById(activityClassId);

        verifyNoInteractions(enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldRejectEnrollmentWhenAuthenticatedUserIdIsNull() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> sut.enroll(customerId, null, List.of()))
                .isInstanceOf(AccessDeniedException.class)
                .hasMessageContaining("You are not allowed to manage enrollments");

        verifyNoInteractions(activityClassRepository);

        verifyNoInteractions(enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldRejectEnrollmentWhenAuthenticatedUserDoesNotOwnCustomer() {

        UUID anotherUserId = UUID.randomUUID();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> sut.enroll(customerId, anotherUserId, List.of()))
        .isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(activityClassRepository);
        verifyNoInteractions(enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldRejectEnrollmentWhenCustomerDoesNotExist() {

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.enroll(customerId, authenticatedUserId, List.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Customer not found");

        verifyNoInteractions(activityClassRepository);
        verifyNoInteractions(enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldCancelActivityWhenAuthenticatedUserOwnsCustomer() {
        UUID enrollmentActivityId = UUID.randomUUID();
        LocalDate endDate = LocalDate.of(2026, 11, 6);

        Enrollment expectedEnrollment = new Enrollment(customer);

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        when(cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate))
                .thenReturn(expectedEnrollment);

        Enrollment result = sut.cancelActivity(customerId, authenticatedUserId, enrollmentActivityId, endDate);
        assertThat(result).isSameAs(expectedEnrollment);

        verify(cancelUseCase).cancelActivity(customer, enrollmentActivityId, endDate);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    void shouldRejectCancellationWhenAuthenticatedUserDoesNotOwnCustomer() {

        UUID anotherUserId = UUID.randomUUID();

        UUID enrollmentActivityId = UUID.randomUUID();

        LocalDate endDate = LocalDate.now();

        when(customerRepository.findById(customerId))
                .thenReturn(Optional.of(customer));

        assertThatThrownBy(() -> sut.cancelActivity(customerId, anotherUserId, enrollmentActivityId, endDate)
        ).isInstanceOf(AccessDeniedException.class);

        verifyNoInteractions(cancelUseCase);
    }

    private Customer createCustomer(UUID customerId, UUID userId) {

        return new Customer(
                customerId,
                userId,
                null,
                "Ana Souza",
                null,
                null,
                null,
                "ana@email.com",
                null,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }

    private ActivityClass createActivityClass(UUID activityClassId) {
        return new ActivityClass(
                activityClassId,
                null,
                null,
                null,
                null,
                10,
                new BigDecimal("100.00"),
                true
        );
    }
}