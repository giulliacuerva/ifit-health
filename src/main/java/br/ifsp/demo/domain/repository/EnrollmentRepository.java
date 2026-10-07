package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.*;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);

    List<EnrollmentActivity> findActivitiesByActivityClass(
            ActivityClass activityClass
    );

    Enrollment findByCustomer(Customer customer);

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