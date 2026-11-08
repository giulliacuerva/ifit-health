package br.ifsp.demo.persistence.sqlite;

import br.ifsp.demo.domain.model.ActivityClass;
import br.ifsp.demo.domain.model.Address;
import br.ifsp.demo.domain.model.Room;
import br.ifsp.demo.domain.model.Schedule;
import br.ifsp.demo.domain.model.Sport;
import br.ifsp.demo.domain.model.Trainer;
import br.ifsp.demo.domain.model.enums.RoomType;
import br.ifsp.demo.domain.repository.ActivityClassRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
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

@Repository
public class SqliteActivityClassRepository implements ActivityClassRepository {

    private static final String SELECT_ACTIVITY = """
            SELECT
                ac.id AS activity_id,
                ac.weekday,
                ac.start_time,
                ac.end_time,
                ac.capacity AS activity_capacity,
                COALESCE(
                    (
                        SELECT ap.monthly_fee
                        FROM activity_prices ap
                        WHERE ap.activity_id = ac.id
                          AND date(ap.valid_from) <= date('now', 'localtime')
                        ORDER BY date(ap.valid_from) DESC, ap.valid_from DESC
                        LIMIT 1
                    ),
                    ac.monthly_fee
                ) AS monthly_fee,
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


    private StoredActivity findStoredActivity(Connection connection, UUID activityId) throws SQLException {
        final String sql = """
                SELECT
                    monthly_fee,
                    date(created_at, 'localtime') AS created_date
                FROM activity_class
                WHERE id = ?;
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, activityId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return null;
                }

                String createdDate = resultSet.getString("created_date");
                LocalDate createdAt = createdDate == null
                        ? LocalDate.now()
                        : LocalDate.parse(createdDate);

                return new StoredActivity(
                        resultSet.getBigDecimal("monthly_fee"),
                        createdAt
                );
            }
        }
    }

    private void ensureInitialPriceHistory(
            Connection connection,
            UUID activityId,
            StoredActivity storedActivity
    ) throws SQLException {
        final String sql = """
                SELECT 1
                FROM activity_prices
                WHERE activity_id = ?
                LIMIT 1;
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, activityId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return;
                }
            }
        }

        insertPriceHistory(
                connection,
                activityId,
                storedActivity.monthlyFee(),
                storedActivity.createdAt()
        );
    }

    private BigDecimal findEffectivePrice(
            Connection connection,
            UUID activityId,
            LocalDate referenceDate,
            BigDecimal fallbackPrice
    ) throws SQLException {
        final String sql = """
                SELECT monthly_fee
                FROM activity_prices
                WHERE activity_id = ?
                  AND date(valid_from) <= ?
                ORDER BY date(valid_from) DESC, valid_from DESC
                LIMIT 1;
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, activityId.toString());
            statement.setString(2, referenceDate.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getBigDecimal("monthly_fee")
                        : fallbackPrice;
            }
        }
    }

    public BigDecimal findLatestPrice(
            UUID activityId,
            BigDecimal fallbackPrice
    ) {
        final String sql = """
            SELECT monthly_fee
            FROM activity_prices
            WHERE activity_id = ?
            ORDER BY date(valid_from) DESC, valid_from DESC
            LIMIT 1;
            """;

        try (Connection connection = ConnectionFactory.createConnection();
             var statement = connection.prepareStatement(sql)) {

            statement.setString(1, activityId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getBigDecimal("monthly_fee")
                        : fallbackPrice;
            }

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Error finding latest activity price",
                    exception
            );
        }
    }

    private BigDecimal findScheduledPrice(
            Connection connection,
            UUID activityId,
            LocalDate effectiveDate
    ) throws SQLException {
        final String sql = """
                SELECT monthly_fee
                FROM activity_prices
                WHERE activity_id = ?
                  AND date(valid_from) = ?
                ORDER BY valid_from DESC
                LIMIT 1;
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, activityId.toString());
            statement.setString(2, effectiveDate.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next()
                        ? resultSet.getBigDecimal("monthly_fee")
                        : null;
            }
        }
    }

    private void insertPriceHistory(
            Connection connection,
            UUID activityId,
            BigDecimal monthlyFee,
            LocalDate validFrom
    ) throws SQLException {
        final String sql = """
                INSERT INTO activity_prices (
                    id,
                    activity_id,
                    monthly_fee,
                    valid_from
                )
                VALUES (?, ?, ?, ?);
                """;

        try (var statement = connection.prepareStatement(sql)) {
            statement.setString(1, UUID.randomUUID().toString());
            statement.setString(2, activityId.toString());
            statement.setBigDecimal(3, monthlyFee);
            statement.setString(4, validFrom.toString());
            statement.executeUpdate();
        }
    }

    private void replacePriceHistory(
            Connection connection,
            UUID activityId,
            BigDecimal monthlyFee,
            LocalDate validFrom
    ) throws SQLException {
        final String deleteSql = """
                DELETE FROM activity_prices
                WHERE activity_id = ?
                  AND date(valid_from) = ?;
                """;

        try (var statement = connection.prepareStatement(deleteSql)) {
            statement.setString(1, activityId.toString());
            statement.setString(2, validFrom.toString());
            statement.executeUpdate();
        }

        insertPriceHistory(connection, activityId, monthlyFee, validFrom);
    }

    private void updatePriceHistory(
            Connection connection,
            UUID activityId,
            BigDecimal requestedPrice,
            BigDecimal currentPrice,
            LocalDate today
    ) throws SQLException {
        LocalDate nextMonthStart = today.withDayOfMonth(1).plusMonths(1);
        BigDecimal scheduledPrice = findScheduledPrice(
                connection,
                activityId,
                nextMonthStart
        );

        if (scheduledPrice != null) {
            if (requestedPrice.compareTo(currentPrice) != 0
                    && requestedPrice.compareTo(scheduledPrice) != 0) {
                replacePriceHistory(
                        connection,
                        activityId,
                        requestedPrice,
                        nextMonthStart
                );
            }
            return;
        }

        if (requestedPrice.compareTo(currentPrice) != 0) {
            insertPriceHistory(
                    connection,
                    activityId,
                    requestedPrice,
                    nextMonthStart
            );
        }
    }

    public Collection<BigDecimal> findPriceHistory(UUID activityId) {
        final Collection<BigDecimal> prices = new ArrayList<>();

        final String sql = """
            SELECT monthly_fee
            FROM activity_prices
            WHERE activity_id = ?
            ORDER BY date(valid_from), valid_from;
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

        BigDecimal requestedPrice = activityClass.getMonthlyFee();
        LocalDate today = LocalDate.now();

        try (Connection connection = ConnectionFactory.createConnection()) {
            connection.setAutoCommit(false);

            try {
                StoredActivity storedActivity = findStoredActivity(
                        connection,
                        activityClass.getId()
                );

                if (storedActivity != null) {
                    ensureInitialPriceHistory(
                            connection,
                            activityClass.getId(),
                            storedActivity
                    );
                }

                try (var statement = connection.prepareStatement(sql)) {
                    statement.setString(1, activityClass.getId().toString());
                    statement.setString(2, activityClass.getSport().getId().toString());
                    statement.setString(3, activityClass.getRoom().getId().toString());
                    statement.setString(4, activityClass.getTrainer().getId().toString());
                    statement.setString(5, serializeWeekdays(activityClass.getSchedule().getWeekdays()));
                    statement.setString(6, activityClass.getSchedule().getStartTime().toString());
                    statement.setString(7, activityClass.getSchedule().getEndTime().toString());
                    statement.setInt(8, activityClass.getCapacity());
                    statement.setBigDecimal(9, requestedPrice);
                    statement.setInt(10, activityClass.isActive() ? 1 : 0);
                    statement.executeUpdate();
                }

                if (storedActivity == null) {
                    insertPriceHistory(
                            connection,
                            activityClass.getId(),
                            requestedPrice,
                            today
                    );
                } else {
                    BigDecimal currentPrice = findEffectivePrice(
                            connection,
                            activityClass.getId(),
                            today,
                            storedActivity.monthlyFee()
                    );

                    updatePriceHistory(
                            connection,
                            activityClass.getId(),
                            requestedPrice,
                            currentPrice,
                            today
                    );

                    BigDecimal effectivePrice = findEffectivePrice(
                            connection,
                            activityClass.getId(),
                            today,
                            storedActivity.monthlyFee()
                    );

                    activityClass.edit(
                            activityClass.getRoom(),
                            activityClass.getSport(),
                            activityClass.getTrainer(),
                            activityClass.getSchedule(),
                            activityClass.getCapacity(),
                            effectivePrice
                    );
                }

                connection.commit();
                return activityClass;

            } catch (SQLException exception) {
                connection.rollback();
                throw exception;
            }

        } catch (SQLException exception) {
            throw new RuntimeException("Error saving activity class", exception);
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

    @Override
    public Room findRoomById(UUID id) {
        final String sql = "SELECT id, name, room_type, capacity FROM rooms WHERE id = ?";
        try (Connection connection = ConnectionFactory.createConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? new Room(UUID.fromString(rs.getString("id")),
                        rs.getString("name"), RoomType.valueOf(rs.getString("room_type")),
                        rs.getInt("capacity")) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding room by id", e);
        }
    }

    @Override
    public Sport findSportById(UUID id) {
        final String sql = "SELECT id, name, description, room_type, active, created_at, updated_at FROM sports WHERE id = ?";
        try (Connection connection = ConnectionFactory.createConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet rs = statement.executeQuery()) {
                return rs.next() ? new Sport(UUID.fromString(rs.getString("id")),
                        rs.getString("name"), rs.getString("description"),
                        RoomType.valueOf(rs.getString("room_type")), rs.getInt("active") == 1,
                        parseDateTime(rs.getString("created_at")),
                        parseDateTime(rs.getString("updated_at"))) : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding sport by id", e);
        }
    }

    @Override
    public Trainer findTrainerById(UUID id) {
        final String sql = "SELECT id, name, email, birthdate, address_cep, address_number, address_street, address_city, address_state, active FROM trainer WHERE id = ?";
        try (Connection connection = ConnectionFactory.createConnection();
             var statement = connection.prepareStatement(sql)) {
            statement.setString(1, id.toString());
            try (ResultSet rs = statement.executeQuery()) {
                if (!rs.next()) return null;
                Address address = mapAddress(rs.getString("address_cep"),
                        rs.getString("address_number"), rs.getString("address_street"),
                        rs.getString("address_city"), rs.getString("address_state"));
                return new Trainer(UUID.fromString(rs.getString("id")), rs.getString("name"),
                        rs.getString("email"), LocalDate.parse(rs.getString("birthdate")),
                        address, rs.getInt("active") == 1);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding trainer by id", e);
        }
    }

    private Address mapAddress(String cep, String number, String street, String city, String state) {
        return cep == null && number == null && street == null && city == null && state == null
                ? null : new Address(cep, number, street, city, state);
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

    private record StoredActivity(BigDecimal monthlyFee, LocalDate createdAt) {
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
