package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    List<EnrollmentActivity> findEnrolledActivitiesByActivityClass(ActivityClass activityClass);
    Optional<Enrollment> findByCustomer(Customer customer);
    List<Enrollment> findEnrollmentsByActivityClass(ActivityClass activityClass);
    List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetween(
            LocalDate startDate,
            LocalDate endDate
    );

    List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetweenAndSport(
            LocalDate startDate,
            LocalDate endDate,
            Sport sport
    );

    List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetweenAndDayOfWeek(
            LocalDate startDate,
            LocalDate endDate,
            DayOfWeek dayOfWeek
    );

    List<EnrollmentActivity> findEnrollmentActivitiesByPeriod(
            LocalDate startDate,
            LocalDate endDate
    );
}
