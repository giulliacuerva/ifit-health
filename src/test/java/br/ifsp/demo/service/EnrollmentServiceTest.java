package br.ifsp.demo.service;

import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.repository.CustomerRepository;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import br.ifsp.demo.domain.usecase.CancelEnrollmentActivityUseCase;
import br.ifsp.demo.domain.usecase.EnrollCustomerUseCase;
import br.ifsp.demo.exception.EnrollmentNotFoundException;
import br.ifsp.demo.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private EnrollCustomerUseCase enrollUseCase;

    @Mock
    private CancelEnrollmentActivityUseCase cancelUseCase;

    @InjectMocks
    private EnrollmentService sut;

    private UUID authenticatedUserId;
    private Customer customer;

    @BeforeEach
    void setUp() {
        UUID customerId = UUID.randomUUID();
        authenticatedUserId = UUID.randomUUID();
        customer = createCustomer(customerId, authenticatedUserId);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should enroll the customer associated with the authenticated user")
    void shouldEnrollCustomerAssociatedWithAuthenticatedUser() {
        List<UUID> activityClassIds = List.of(UUID.randomUUID(), UUID.randomUUID());
        Enrollment expectedEnrollment = new Enrollment(customer);

        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.of(customer));
        when(enrollUseCase.enroll(customer, activityClassIds))
                .thenReturn(expectedEnrollment);

        Enrollment result = sut.enroll(authenticatedUserId, activityClassIds);

        assertThat(result).isSameAs(expectedEnrollment);

        verify(customerRepository).findByUserId(authenticatedUserId);
        verify(customerRepository, never()).findById(any(UUID.class));
        verify(enrollUseCase).enroll(customer, activityClassIds);
        verifyNoInteractions(enrollmentRepository, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when the authenticated user has no customer profile")
    void shouldRejectEnrollmentWhenAuthenticatedUserHasNoCustomerProfile() {
        List<UUID> activityClassIds = List.of(UUID.randomUUID());

        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.enroll(authenticatedUserId, activityClassIds))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No student profile is associated with the authenticated user.");

        verify(customerRepository).findByUserId(authenticatedUserId);
        verifyNoInteractions(enrollUseCase, enrollmentRepository, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment when authenticated user ID is null")
    void shouldRejectEnrollmentWhenAuthenticatedUserIdIsNull() {
        assertThatThrownBy(() -> sut.enroll(null, List.of(UUID.randomUUID())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user could not be identified.");

        verifyNoInteractions(customerRepository, enrollmentRepository, enrollUseCase, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should return the enrollment associated with the authenticated user")
    void shouldFindEnrollmentForAuthenticatedUser() {
        Enrollment expectedEnrollment = new Enrollment(customer);

        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.of(customer));
        when(enrollmentRepository.findByCustomer(customer))
                .thenReturn(Optional.of(expectedEnrollment));

        Enrollment result = sut.findMyEnrollment(authenticatedUserId);

        assertThat(result).isSameAs(expectedEnrollment);

        verify(customerRepository).findByUserId(authenticatedUserId);
        verify(enrollmentRepository).findByCustomer(customer);
        verifyNoInteractions(enrollUseCase, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment lookup when the customer has no enrollment")
    void shouldRejectEnrollmentLookupWhenCustomerHasNoEnrollment() {
        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.of(customer));
        when(enrollmentRepository.findByCustomer(customer))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findMyEnrollment(authenticatedUserId))
                .isInstanceOf(EnrollmentNotFoundException.class)
                .hasMessage("No enrollment was found for the authenticated student.");

        verify(customerRepository).findByUserId(authenticatedUserId);
        verify(enrollmentRepository).findByCustomer(customer);
        verifyNoInteractions(enrollUseCase, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject enrollment lookup when authenticated user has no customer profile")
    void shouldRejectEnrollmentLookupWhenAuthenticatedUserHasNoCustomerProfile() {
        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.findMyEnrollment(authenticatedUserId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No student profile is associated with the authenticated user.");

        verify(customerRepository).findByUserId(authenticatedUserId);
        verifyNoInteractions(enrollmentRepository, enrollUseCase, cancelUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should cancel an activity from the authenticated customer's enrollment")
    void shouldCancelActivityForAuthenticatedUser() {
        UUID enrollmentActivityId = UUID.randomUUID();
        LocalDate endDate = LocalDate.of(2026, 11, 6);
        Enrollment expectedEnrollment = new Enrollment(customer);

        when(customerRepository.findByUserId(authenticatedUserId))
                .thenReturn(Optional.of(customer));
        when(cancelUseCase.cancelActivity(customer, enrollmentActivityId, endDate))
                .thenReturn(expectedEnrollment);

        Enrollment result = sut.cancelActivity(authenticatedUserId, enrollmentActivityId, endDate);

        assertThat(result).isSameAs(expectedEnrollment);

        verify(customerRepository).findByUserId(authenticatedUserId);
        verify(customerRepository, never()).findById(any(UUID.class));
        verify(cancelUseCase).cancelActivity(customer, enrollmentActivityId, endDate);
        verifyNoInteractions(enrollmentRepository, enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject cancellation when authenticated user has no customer profile")
    void shouldRejectCancellationWhenAuthenticatedUserHasNoCustomerProfile() {
        UUID enrollmentActivityId = UUID.randomUUID();

        when(customerRepository.findByUserId(authenticatedUserId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> sut.cancelActivity(authenticatedUserId, enrollmentActivityId, LocalDate.now()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("No student profile is associated with the authenticated user.");

        verify(customerRepository).findByUserId(authenticatedUserId);
        verifyNoInteractions(cancelUseCase, enrollmentRepository, enrollUseCase);
    }

    @Test
    @Tag("Functional")
    @Tag("UnitTest")
    @DisplayName("Should reject cancellation when authenticated user ID is null")
    void shouldRejectCancellationWhenAuthenticatedUserIdIsNull() {
        assertThatThrownBy(() -> sut.cancelActivity(
                null,
                UUID.randomUUID(),
                LocalDate.now()
        ))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Authenticated user could not be identified.");

        verifyNoInteractions(customerRepository, enrollmentRepository, enrollUseCase, cancelUseCase);
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
}
