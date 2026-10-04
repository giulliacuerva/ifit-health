package br.ifsp.demo.domain.repository;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.domain.model.Enrollment;
import br.ifsp.demo.domain.model.EnrollmentActivity;

import java.util.List;

public interface EnrollmentRepository {
    Enrollment save(Enrollment enrollment);
    List<EnrollmentActivity> findActivitiesByActivityClass(ActivityClass activityClass);
    Enrollment findByCustomer(Customer customer);
}
