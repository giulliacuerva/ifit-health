package br.ifsp.demo.support;

import br.ifsp.demo.persistence.sqlite.ConnectionFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Arrays;
import java.util.UUID;
import java.util.stream.Collectors;

public final class DbFixtures {
    private DbFixtures() {
    }

    public static UUID insertActivity(UUID sportId, UUID roomId, UUID trainerId, LocalTime start, LocalTime end,
                                      int capacity, BigDecimal fee, boolean active, DayOfWeek... weekdays) {
        UUID id = UUID.randomUUID();
        update("""
                INSERT INTO activity_class (id, sport_id, room_id, coach_id, weekday, start_time, end_time,
                                            capacity, monthly_fee, active)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """, id.toString(), sportId.toString(), roomId.toString(), trainerId.toString(), serialize(weekdays),
                start.toString(), end.toString(), capacity, fee, active ? 1 : 0);
        return id;
    }

    public static void insertPrice(UUID activityId, BigDecimal fee, LocalDate validFrom) {
        update("INSERT INTO activity_prices (id, activity_id, monthly_fee, valid_from) VALUES (?, ?, ?, ?)",
                UUID.randomUUID().toString(), activityId.toString(), fee, validFrom.toString());
    }

    public static UUID insertCustomer(String name, String cpf, String email) {
        return insertCustomer(null, name, cpf, email);
    }

    public static UUID insertCustomer(UUID userId, String name, String cpf, String email) {
        UUID id = UUID.randomUUID();
        update("""
                INSERT INTO customers (id, user_id, cpf, name, birthdate, gender, email)
                VALUES (?, ?, ?, ?, '2000-01-01', 'FEMALE', ?)
                """, id.toString(), userId == null ? null : userId.toString(), cpf, name, email);
        return id;
    }

    public static UUID insertCustomer() {
        return insertCustomer(FakeData.firstName() + " " + FakeData.lastName(), FakeData.cpf(), FakeData.email());
    }

    public static UUID insertEnrollment(UUID customerId, boolean active) {
        UUID id = UUID.randomUUID();
        update("INSERT INTO enrollments (id, customer_id, enroll_date, status) VALUES (?, ?, ?, ?)",
                id.toString(), customerId.toString(), LocalDate.now().toString(), active ? "ACTIVE" : "INACTIVE");
        return id;
    }

    public static UUID insertEnrollmentActivity(UUID enrollmentId, UUID activityId, BigDecimal fee,
                                                LocalDate start, LocalDate end, boolean active) {
        UUID id = UUID.randomUUID();
        update("""
                INSERT INTO enrollment_activities (id, enrollment_id, activity_id, monthly_fee, start_date, end_date, status)
                VALUES (?, ?, ?, ?, ?, ?, ?)
                """, id.toString(), enrollmentId.toString(), activityId.toString(), fee, start.toString(),
                end == null ? null : end.toString(), active ? "ACTIVE" : "INACTIVE");
        return id;
    }

    public static UUID enrollNewCustomer(UUID activityId, BigDecimal fee, LocalDate start) {
        UUID enrollment = insertEnrollment(insertCustomer(), true);
        return insertEnrollmentActivity(enrollment, activityId, fee, start, null, true);
    }

    public static void fillActivity(UUID activityId, BigDecimal fee, int students) {
        for (int i = 0; i < students; i++) {
            enrollNewCustomer(activityId, fee, LocalDate.now());
        }
    }

    public static int count(String sql, Object... parameters) {
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = prepare(connection, sql, parameters);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getInt(1) : 0;
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static String text(String sql, Object... parameters) {
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = prepare(connection, sql, parameters);
             ResultSet resultSet = statement.executeQuery()) {
            return resultSet.next() ? resultSet.getString(1) : null;
        } catch (SQLException exception) {
            throw new IllegalStateException(exception);
        }
    }

    public static int execute(String sql, Object... parameters) throws SQLException {
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = prepare(connection, sql, parameters)) {
            return statement.executeUpdate();
        }
    }

    public static int update(String sql, Object... parameters) {
        try {
            return execute(sql, parameters);
        } catch (SQLException exception) {
            throw new IllegalStateException("Falha ao preparar o banco: " + sql, exception);
        }
    }

    private static PreparedStatement prepare(Connection connection, String sql, Object... parameters) throws SQLException {
        PreparedStatement statement = connection.prepareStatement(sql);
        for (int i = 0; i < parameters.length; i++) {
            statement.setObject(i + 1, parameters[i] instanceof BigDecimal decimal ? decimal.toPlainString() : parameters[i]);
        }
        return statement;
    }

    private static String serialize(DayOfWeek... weekdays) {
        return Arrays.stream(weekdays).sorted().map(DayOfWeek::name).collect(Collectors.joining(","));
    }
}
