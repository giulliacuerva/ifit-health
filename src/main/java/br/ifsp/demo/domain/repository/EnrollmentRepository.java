package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.EnrollmentActivity;

import java.util.List;
import java.util.Optional;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    List<EnrollmentActivity> findEnrolledActivitiesByActivityClass(ActivityClass activityClass);
    Optional<Enrollment> findByCustomer(Customer customer);
}
