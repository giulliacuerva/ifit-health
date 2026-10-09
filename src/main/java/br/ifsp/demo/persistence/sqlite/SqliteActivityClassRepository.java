package br.ifsp.demo.persistence.sqlite;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Address;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.math.BigDecimal;
import java.time.LocalDateTime;

public class SqliteActivityClassRepository implements ActivityClassRepository {

    private static final String SELECT_ACTIVITY = """
            SELECT
                ac.id AS activity_id,
                ac.weekday,
                ac.start_time,
                ac.end_time,
                ac.capacity AS activity_capacity,
                ac.monthly_fee,
                ac.active AS activity_active,

                r.id AS room_id,
                r.name AS room_name,
                r.room_type AS room_type,
                r.capacity AS room_capacity,

                s.id AS sport_id,
                s.name AS sport_name,
                s.description AS sport_description,
                s.room_type AS sport_room_type,
                s.active AS sport_active,
                s.created_at AS sport_created_at,
                s.updated_at AS sport_updated_at,

                t.id AS trainer_id,
                t.name AS trainer_name,
                t.email AS trainer_email,
                t.birthdate AS trainer_birthdate,
                t.address_cep AS trainer_address_cep,
                t.address_number AS trainer_address_number,
                t.address_street AS trainer_address_street,
                t.address_city AS trainer_address_city,
                t.address_state AS trainer_address_state,
                t.active AS trainer_active

            FROM activity_class ac
            JOIN rooms r ON r.id = ac.room_id
            JOIN sports s ON s.id = ac.sport_id
            JOIN trainer t ON t.id = ac.coach_id
            """;


    private BigDecimal findCurrentPrice(Connection connection, UUID activityId) throws SQLException {
        final String sql = """
            SELECT monthly_fee
            FROM activity_class
            WHERE id = ?;
            """;

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, activityId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getBigDecimal("monthly_fee");
                }

                return null;
            }
        }
    }

    private void savePriceHistory(Connection connection, ActivityClass activityClass) throws SQLException {
        final String sql = """
            INSERT INTO activity_prices (
                id,
                activity_id,
                monthly_fee,
                valid_from
            )
            VALUES (?, ?, ?, ?);
            """;

        UUID priceHistoryId = UUID.randomUUID();

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, priceHistoryId.toString());
            stmt.setString(2, activityClass.getId().toString());
            stmt.setBigDecimal(3, activityClass.getMonthlyFee());
            stmt.setString(4, LocalDateTime.now().toString());

            stmt.executeUpdate();
        }
    }

    public Collection<BigDecimal> findPriceHistory(UUID activityId) {
        final Collection<BigDecimal> prices = new ArrayList<>();

        final String sql = """
            SELECT monthly_fee
            FROM activity_prices
            WHERE activity_id = ?
            ORDER BY valid_from;
            """;

        try (Connection connection = ConnectionFactory.createConnection();
             var stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, activityId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(rs.getBigDecimal("monthly_fee"));
                }
            }

            return prices;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding activity price history", e);
        }
    }

    @Override
    public ActivityClass save(ActivityClass activityClass) {
        final String sql = """
            INSERT INTO activity_class (
                id,
                sport_id,
                room_id,
                coach_id,
                weekday,
                start_time,
                end_time,
                capacity,
                monthly_fee,
                active
            )
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
            ON CONFLICT(id) DO UPDATE SET
                sport_id = excluded.sport_id,
                room_id = excluded.room_id,
                coach_id = excluded.coach_id,
                weekday = excluded.weekday,
                start_time = excluded.start_time,
                end_time = excluded.end_time,
                capacity = excluded.capacity,
                monthly_fee = excluded.monthly_fee,
                active = excluded.active,
                updated_at = CURRENT_TIMESTAMP;
            """;

        try (Connection connection = ConnectionFactory.createConnection()) {
            connection.setAutoCommit(false);

            try {
                BigDecimal previousPrice = findCurrentPrice(connection, activityClass.getId());

                try (var stmt = connection.prepareStatement(sql)) {
                    stmt.setString(1, activityClass.getId().toString());
                    stmt.setString(2, activityClass.getSport().getId().toString());
                    stmt.setString(3, activityClass.getRoom().getId().toString());
                    stmt.setString(4, activityClass.getTrainer().getId().toString());
                    stmt.setString(5, serializeWeekdays(activityClass.getSchedule().getWeekdays()));
                    stmt.setString(6, activityClass.getSchedule().getStartTime().toString());
                    stmt.setString(7, activityClass.getSchedule().getEndTime().toString());
                    stmt.setInt(8, activityClass.getCapacity());
                    stmt.setBigDecimal(9, activityClass.getMonthlyFee());
                    stmt.setInt(10, activityClass.isActive() ? 1 : 0);

                    stmt.executeUpdate();
                }

                if (previousPrice == null || previousPrice.compareTo(activityClass.getMonthlyFee()) != 0) {
                    savePriceHistory(connection, activityClass);
                }

                connection.commit();

                return activityClass;

            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }

        } catch (SQLException e) {
            throw new RuntimeException("Error saving activity class", e);
        }
    }

    @Override
    public ActivityClass findById(UUID activityClassId) {
        final String sql = SELECT_ACTIVITY + """
                WHERE ac.id = ?
                """;

        try (Connection connection = ConnectionFactory.createConnection();
             var stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, activityClassId.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapActivityClass(rs);
                }
            }

            return null;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding activity class by id", e);
        }
    }

    @Override
    public Collection<ActivityClass> findByRoom(Room room) {
        final Collection<ActivityClass> activities = new ArrayList<>();

        final String sql = SELECT_ACTIVITY + """
                WHERE ac.room_id = ?
                  AND ac.active = 1
                """;

        try (Connection connection = ConnectionFactory.createConnection();
             var stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, room.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivityClass(rs));
                }
            }

            return activities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding activity classes by room", e);
        }
    }

    @Override
    public Collection<ActivityClass> findByTrainer(Trainer trainer) {
        final Collection<ActivityClass> activities = new ArrayList<>();

        final String sql = SELECT_ACTIVITY + """
                WHERE ac.coach_id = ?
                  AND ac.active = 1
                """;

        try (Connection connection = ConnectionFactory.createConnection();
             var stmt = connection.prepareStatement(sql)) {

            stmt.setString(1, trainer.getId().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    activities.add(mapActivityClass(rs));
                }
            }

            return activities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding activity classes by trainer", e);
        }
    }

    @Override
    public Collection<ActivityClass> findAll() {
        final Collection<ActivityClass> activities = new ArrayList<>();

        final String sql = SELECT_ACTIVITY + """
                ORDER BY ac.id
                """;

        try (Connection connection = ConnectionFactory.createConnection();
             var stmt = connection.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                activities.add(mapActivityClass(rs));
            }

            return activities;

        } catch (SQLException e) {
            throw new RuntimeException("Error finding all activity classes", e);
        }
    }

    private ActivityClass mapActivityClass(ResultSet rs) throws SQLException {
        Room room = new Room(
                UUID.fromString(rs.getString("room_id")),
                rs.getString("room_name"),
                RoomType.valueOf(rs.getString("room_type")),
                rs.getInt("room_capacity")
        );

        Sport sport = new Sport(
                UUID.fromString(rs.getString("sport_id")),
                rs.getString("sport_name"),
                rs.getString("sport_description"),
                RoomType.valueOf(rs.getString("sport_room_type")),
                rs.getInt("sport_active") == 1,
                parseDateTime(rs.getString("sport_created_at")),
                parseDateTime(rs.getString("sport_updated_at"))
        );

        Address trainerAddress = mapTrainerAddress(rs);

        Trainer trainer = new Trainer(
                UUID.fromString(rs.getString("trainer_id")),
                rs.getString("trainer_name"),
                rs.getString("trainer_email"),
                LocalDate.parse(rs.getString("trainer_birthdate")),
                trainerAddress,
                rs.getInt("trainer_active") == 1
        );

        Set<DayOfWeek> weekdays = deserializeWeekdays(rs.getString("weekday"));

        Schedule schedule = new Schedule(
                weekdays,
                LocalTime.parse(rs.getString("start_time")),
                LocalTime.parse(rs.getString("end_time"))
        );

        return new ActivityClass(
                UUID.fromString(rs.getString("activity_id")),
                room,
                sport,
                trainer,
                schedule,
                rs.getInt("activity_capacity"),
                rs.getBigDecimal("monthly_fee"),
                rs.getInt("activity_active") == 1
        );
    }

    private Address mapTrainerAddress(ResultSet rs) throws SQLException {
        String cep = rs.getString("trainer_address_cep");
        String number = rs.getString("trainer_address_number");
        String street = rs.getString("trainer_address_street");
        String city = rs.getString("trainer_address_city");
        String state = rs.getString("trainer_address_state");

        if (cep == null && number == null && street == null && city == null && state == null) {
            return null;
        }

        return new Address(cep, number, street, city, state);
    }

    private String serializeWeekdays(Set<DayOfWeek> weekdays) {
        return weekdays.stream().sorted(Comparator.comparingInt(DayOfWeek::getValue)).map(DayOfWeek::name).collect(Collectors.joining(","));
    }

    private Set<DayOfWeek> deserializeWeekdays(String weekdays) {
        return Arrays.stream(weekdays.split(",")).map(String::trim).map(DayOfWeek::valueOf).collect(Collectors.toCollection(() -> EnumSet.noneOf(DayOfWeek.class)));
    }

    private LocalDateTime parseDateTime(String value) {
        return value == null ? null : LocalDateTime.parse(value.replace(" ", "T"));
    }
}