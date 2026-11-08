package br.ifsp.demo.persistence.sqlite;

import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SqliteActivityClassQueries {

    private static final String EFFECTIVE_PRICE_SQL = """
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
            )
            """;

    private static final String LATEST_PRICE_SQL = """
            COALESCE(
                (
                    SELECT ap.monthly_fee
                    FROM activity_prices ap
                    WHERE ap.activity_id = ac.id
                    ORDER BY date(ap.valid_from) DESC, ap.valid_from DESC
                    LIMIT 1
                ),
                ac.monthly_fee
            )
            """;

    private String buildFindAllSql(boolean showLatestPrice) {

        String priceSql = showLatestPrice
                ? LATEST_PRICE_SQL
                : EFFECTIVE_PRICE_SQL;

        return """
                WITH active_enrollment_counts AS (
                    SELECT
                        ea.activity_id,
                        COUNT(DISTINCT e.customer_id) AS enrolled_count
                    FROM enrollment_activities ea
                    JOIN enrollments e ON e.id = ea.enrollment_id
                    WHERE ea.status = 'ACTIVE'
                      AND e.status = 'ACTIVE'
                    GROUP BY ea.activity_id
                )
                SELECT
                    ac.id AS activity_id,
                    ac.sport_id,
                    s.name AS sport_name,
                    ac.room_id,
                    r.name AS room_name,
                    ac.coach_id AS trainer_id,
                    t.name AS trainer_name,
                    ac.weekday,
                    ac.start_time,
                    ac.end_time,
                    ac.capacity,
                    COALESCE(aec.enrolled_count, 0) AS enrolled_count,
                    %s AS monthly_fee,
                    ac.active
                FROM activity_class ac
                JOIN sports s ON s.id = ac.sport_id
                JOIN rooms r ON r.id = ac.room_id
                JOIN trainer t ON t.id = ac.coach_id
                LEFT JOIN active_enrollment_counts aec
                    ON aec.activity_id = ac.id
                """.formatted(priceSql);
    }

    public List<ActivityClassRow> findAll(Boolean active) {
        return findAll(active, false);
    }

    public List<ActivityClassRow> findAll(
            Boolean active,
            boolean showLatestPrice
    ) {

        StringBuilder sql = new StringBuilder(
                buildFindAllSql(showLatestPrice)
        );

        if (active != null) {
            sql.append(" WHERE ac.active = ?\n");
        }

        sql.append(" ORDER BY s.name COLLATE NOCASE, ac.id");

        List<ActivityClassRow> activities = new ArrayList<>();

        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement =
                     connection.prepareStatement(sql.toString())) {

            if (active != null) {
                statement.setInt(1, active ? 1 : 0);
            }

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    activities.add(mapRow(resultSet));
                }
            }

            return activities;

        } catch (SQLException exception) {
            throw new RuntimeException(
                    "Error finding activity classes with availability",
                    exception
            );
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
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

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

    private ActivityClassRow mapRow(ResultSet resultSet)
            throws SQLException {

        return new ActivityClassRow(
                UUID.fromString(resultSet.getString("activity_id")),
                UUID.fromString(resultSet.getString("sport_id")),
                resultSet.getString("sport_name"),
                UUID.fromString(resultSet.getString("room_id")),
                resultSet.getString("room_name"),
                UUID.fromString(resultSet.getString("trainer_id")),
                resultSet.getString("trainer_name"),
                resultSet.getString("weekday"),
                LocalTime.parse(resultSet.getString("start_time")),
                LocalTime.parse(resultSet.getString("end_time")),
                resultSet.getInt("capacity"),
                resultSet.getInt("enrolled_count"),
                resultSet.getBigDecimal("monthly_fee"),
                resultSet.getInt("active") == 1
        );
    }

    public record ActivityClassRow(
            UUID id,
            UUID sportId,
            String sportName,
            UUID roomId,
            String roomName,
            UUID trainerId,
            String trainerName,
            String weekdays,
            LocalTime startTime,
            LocalTime endTime,
            int capacity,
            int enrolledCount,
            BigDecimal monthlyFee,
            boolean active
    ) {
    }
}