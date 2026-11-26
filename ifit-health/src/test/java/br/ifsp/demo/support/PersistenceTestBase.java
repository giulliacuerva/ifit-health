package br.ifsp.demo.support;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Customer;
import br.ifsp.demo.persistence.sqlite.SqliteActivityClassRepository;
import br.ifsp.demo.persistence.sqlite.SqliteCustomerRepository;
import org.assertj.core.api.AbstractThrowableAssert;
import org.springframework.beans.factory.annotation.Autowired;

import java.sql.SQLException;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

public abstract class PersistenceTestBase extends IntegrationTestBase {
    @Autowired
    protected SqliteActivityClassRepository activityClassRepository;

    @Autowired
    protected SqliteCustomerRepository customerRepository;

    protected ActivityClass activity(UUID id) {
        return activityClassRepository.findById(id);
    }

    protected Customer customer(UUID id) {
        return customerRepository.findById(id).orElseThrow();
    }

    protected static AbstractThrowableAssert<?, ? extends Throwable> assertRejected(String sql, Object... parameters) {
        return assertThatThrownBy(() -> DbFixtures.execute(sql, parameters)).isInstanceOf(SQLException.class);
    }
}
