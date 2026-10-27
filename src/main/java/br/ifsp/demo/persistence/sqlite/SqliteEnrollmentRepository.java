package br.ifsp.demo.persistence.sqlite;

import br.ifsp.demo.domain.model.*;
import br.ifsp.demo.domain.model.enums.Gender;
import br.ifsp.demo.domain.repository.EnrollmentRepository;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public class SqliteEnrollmentRepository implements EnrollmentRepository {

    private final SqliteActivityClassRepository activityClassRepository;

    public SqliteEnrollmentRepository(SqliteActivityClassRepository activityClassRepository) {
        this.activityClassRepository = activityClassRepository;
    }

    @Override
    public Optional<Customer> findCustomerById(UUID customerId) {
        final String sql = "SELECT id AS customer_id, user_id, cpf, name AS customer_name, birthdate, gender, tel, email, address_cep, address_number, address_street, address_city, address_state, created_at, updated_at FROM customers WHERE id = ?";
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, customerId.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? Optional.of(mapCustomer(rs)) : Optional.empty();
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding customer by id", e);
        }
    }

    @Override
    public Enrollment save(Enrollment enrollment) {
        try (Connection connection = ConnectionFactory.createConnection()) {
            connection.setAutoCommit(false);

            try {
                saveEnrollment(connection, enrollment);

                for (EnrollmentActivity enrollmentActivity : enrollment.getEnrollmentActivities()) {
                    saveEnrollmentActivity(connection, enrollment.getId(), enrollmentActivity);
                }

                connection.commit();
                return enrollment;
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving enrollment", e);
        }
    }

    @Override
    public Optional<Enrollment> findByCustomer(Customer customer) {
        final String sql = "SELECT id, status " +
                            "FROM enrollments " +
                            "WHERE customer_id = ?;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, customer.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (!rs.next()) return Optional.empty();

                UUID enrollmentId = UUID.fromString(rs.getString("id"));
                boolean active = "ACTIVE".equals(rs.getString("status"));
                List<EnrollmentActivity> enrollmentActivities = findEnrollmentActivitiesByEnrollmentId(enrollmentId);

                return Optional.of(new Enrollment(enrollmentId, customer, enrollmentActivities, active));
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error finding enrollment by customer", e);
        }
    }

    @Override
    public List<EnrollmentActivity> findEnrolledActivitiesByActivityClass(ActivityClass activityClass) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT ea.id, ea.activity_id, ea.monthly_fee, ea.start_date, ea.end_date, ea.status " +
                            "FROM enrollment_activities ea JOIN enrollments e ON e.id = ea.enrollment_id " +
                            "WHERE ea.activity_id = ? AND ea.status = 'ACTIVE' AND e.status = 'ACTIVE';";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityClass.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs, activityClass));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding enrolled activities by activity class", e);
        }
    }

    @Override
    public List<Enrollment> findEnrollmentsByActivityClass(ActivityClass activityClass) {
        final List<Enrollment> enrollments = new ArrayList<>();
        final String sql = "SELECT DISTINCT e.id AS enrollment_id, e.status AS enrollment_status, c.id AS customer_id, c.user_id, c.cpf, c.name " +
                            "AS customer_name, c.birthdate, c.gender, c.tel, c.email, c.address_cep, c.address_number, c.address_street, c.address_city, c.address_state, c.created_at, c.updated_at " +
                            "FROM enrollments e JOIN enrollment_activities ea ON ea.enrollment_id = e.id JOIN customers c ON c.id = e.customer_id " +
                            "WHERE ea.activity_id = ? AND ea.status = 'ACTIVE' AND e.status = 'ACTIVE';";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityClass.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    UUID enrollmentId = UUID.fromString(rs.getString("enrollment_id"));
                    Customer customer = mapCustomer(rs);
                    List<EnrollmentActivity> enrollmentActivities = findEnrollmentActivitiesByEnrollmentId(enrollmentId);
                    enrollments.add(new Enrollment(enrollmentId, customer, enrollmentActivities, "ACTIVE".equals(rs.getString("enrollment_status"))));
                }
            }

            return enrollments;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding enrollments by activity class", e);
        }
    }

    @Override
    public List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetween(LocalDate startDate, LocalDate endDate) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT ea.id, ea.activity_id, ea.monthly_fee, ea.start_date, ea.end_date, ea.status " +
                            "FROM enrollment_activities ea " +
                            "JOIN enrollments e ON e.id = ea.enrollment_id " +
                            "WHERE ea.status = 'ACTIVE' AND e.status = 'ACTIVE' AND ea.start_date BETWEEN ? AND ? ORDER BY ea.start_date;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding active enrollment activities by start date", e);
        }
    }

    @Override
    public List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetweenAndSport(LocalDate startDate, LocalDate endDate, Sport sport) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT ea.id, ea.activity_id, ea.monthly_fee, ea.start_date, ea.end_date, ea.status " +
                            "FROM enrollment_activities ea " +
                            "JOIN enrollments e ON e.id = ea.enrollment_id JOIN activity_class ac ON ac.id = ea.activity_id " +
                            "WHERE ea.status = 'ACTIVE' AND e.status = 'ACTIVE' AND ea.start_date BETWEEN ? AND ? AND ac.sport_id = ? ORDER BY ea.start_date;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            stmt.setString(3, sport.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding active enrollment activities by sport", e);
        }
    }

    @Override
    public List<EnrollmentActivity> findActiveEnrollmentActivitiesByStartDateBetweenAndDayOfWeek(LocalDate startDate, LocalDate endDate, DayOfWeek dayOfWeek) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT ea.id, ea.activity_id, ea.monthly_fee, ea.start_date, ea.end_date, ea.status " +
                            "FROM enrollment_activities ea " +
                            "JOIN enrollments e ON e.id = ea.enrollment_id JOIN activity_class ac ON ac.id = ea.activity_id " +
                            "WHERE ea.status = 'ACTIVE' AND e.status = 'ACTIVE' AND ea.start_date BETWEEN ? AND ? AND instr(',' || ac.weekday || ',', ',' || ? || ',') > 0 ORDER BY ea.start_date;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, startDate.toString());
            stmt.setString(2, endDate.toString());
            stmt.setString(3, dayOfWeek.name());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding active enrollment activities by day of week", e);
        }
    }

    @Override
    public List<EnrollmentActivity> findEnrollmentActivitiesByPeriod(LocalDate startDate, LocalDate endDate) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT ea.id, ea.activity_id, ea.monthly_fee, ea.start_date, ea.end_date, ea.status " +
                            "FROM enrollment_activities ea " +
                            "WHERE ea.start_date <= ? AND (ea.end_date IS NULL OR ea.end_date >= ?) " +
                            "ORDER BY ea.start_date;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, endDate.toString());
            stmt.setString(2, startDate.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding enrollment activities by period", e);
        }
    }

    private void saveEnrollment(Connection connection, Enrollment enrollment) throws SQLException {
        final String sql = "INSERT INTO enrollments (id, customer_id, enroll_date, end_date, status) " +
                            "VALUES (?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET customer_id = excluded.customer_id, end_date = CASE WHEN excluded.status = 'ACTIVE' " +
                            "THEN NULL WHEN enrollments.end_date IS NULL THEN excluded.end_date ELSE enrollments.end_date END, status = excluded.status, updated_at = CURRENT_TIMESTAMP;";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, enrollment.getId().toString());
            stmt.setString(2, enrollment.getCustomer().getId().toString());
            stmt.setString(3, LocalDate.now().toString());

            if (enrollment.isActive()) stmt.setNull(4, Types.VARCHAR);
            else stmt.setString(4, LocalDate.now().toString());

            stmt.setString(5, enrollment.isActive() ? "ACTIVE" : "INACTIVE");
            stmt.executeUpdate();
        }
    }

    private void saveEnrollmentActivity(Connection connection, UUID enrollmentId, EnrollmentActivity enrollmentActivity) throws SQLException {
        final String sql = "INSERT INTO enrollment_activities (id, enrollment_id, activity_id, monthly_fee, start_date, end_date, status) " +
                            "VALUES (?, ?, ?, ?, ?, ?, ?) ON CONFLICT(id) DO UPDATE SET end_date = excluded.end_date, status = excluded.status;";

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, enrollmentActivity.getId().toString());
            stmt.setString(2, enrollmentId.toString());
            stmt.setString(3, enrollmentActivity.getActivityClass().getId().toString());
            stmt.setBigDecimal(4, enrollmentActivity.getMonthlyFee());
            stmt.setString(5, enrollmentActivity.getStartDate().toString());

            if (enrollmentActivity.getEndDate() == null) stmt.setNull(6, Types.VARCHAR);
            else stmt.setString(6, enrollmentActivity.getEndDate().toString());

            stmt.setString(7, enrollmentActivity.isActive() ? "ACTIVE" : "INACTIVE");
            stmt.executeUpdate();
        }
    }

    private List<EnrollmentActivity> findEnrollmentActivitiesByEnrollmentId(UUID enrollmentId) {
        final List<EnrollmentActivity> enrollmentActivities = new ArrayList<>();
        final String sql = "SELECT id, activity_id, monthly_fee, start_date, end_date, status " +
                            "FROM enrollment_activities " +
                            "WHERE enrollment_id = ? ORDER BY start_date;";

        try (Connection connection = ConnectionFactory.createConnection(); PreparedStatement stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, enrollmentId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) enrollmentActivities.add(mapEnrollmentActivity(rs));
            }

            return enrollmentActivities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding enrollment activities", e);
        }
    }

    private EnrollmentActivity mapEnrollmentActivity(ResultSet rs) throws SQLException {
        UUID activityId = UUID.fromString(rs.getString("activity_id"));
        ActivityClass activityClass = activityClassRepository.findById(activityId);

        if (activityClass == null) throw new IllegalStateException("Activity class not found: " + activityId);

        return mapEnrollmentActivity(rs, activityClass);
    }

    private EnrollmentActivity mapEnrollmentActivity(ResultSet rs, ActivityClass activityClass) throws SQLException {
        return new EnrollmentActivity(UUID.fromString(rs.getString("id")), activityClass, rs.getBigDecimal("monthly_fee"), "ACTIVE".equals(rs.getString("status")), LocalDate.parse(rs.getString("start_date")), parseDate(rs.getString("end_date")));
    }

    private Customer mapCustomer(ResultSet rs) throws SQLException {
        Address address = mapCustomerAddress(rs);
        String userIdValue = rs.getString("user_id");
        UUID userId = userIdValue == null ? null : UUID.fromString(userIdValue);

        return new Customer(UUID.fromString(rs.getString("customer_id")), userId, rs.getString("cpf"), rs.getString("customer_name"), LocalDate.parse(rs.getString("birthdate")), Gender.valueOf(rs.getString("gender")), rs.getString("tel"), rs.getString("email"), address, parseDateTime(rs.getString("created_at")), parseDateTime(rs.getString("updated_at")));
    }

    private Address mapCustomerAddress(ResultSet rs) throws SQLException {
        String cep = rs.getString("address_cep");
        String number = rs.getString("address_number");
        String street = rs.getString("address_street");
        String city = rs.getString("address_city");
        String state = rs.getString("address_state");

        if (cep == null && number == null && street == null && city == null && state == null) return null;

        return new Address(cep, number, street, city, state);
    }

    private LocalDate parseDate(String value) {
        return value == null ? null : LocalDate.parse(value);
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value.replace(" ", "T"));
    }
}
