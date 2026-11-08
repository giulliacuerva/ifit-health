package br.ifsp.demo.persistence.sqlite;

import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


@Repository
public class SqliteScheduleConflictQueries {

    private static final String FIND_ACTIVE_ACTIVITIES_SQL = """
            SELECT DISTINCT
                c.id AS customer_id,
                c.name AS customer_name,
                other_activity.id AS activity_class_id,
                other_sport.name AS sport_name,
                other_activity.weekday AS weekdays,
                other_activity.start_time,
                other_activity.end_time
            FROM enrollments e
            JOIN customers c ON c.id = e.customer_id
            JOIN enrollment_activities target_enrollment
                ON target_enrollment.enrollment_id = e.id
            JOIN enrollment_activities other_enrollment
                ON other_enrollment.enrollment_id = e.id
            JOIN activity_class other_activity
                ON other_activity.id = other_enrollment.activity_id
            JOIN sports other_sport
                ON other_sport.id = other_activity.sport_id
            WHERE target_enrollment.activity_id = ?
              AND target_enrollment.status = 'ACTIVE'
              AND e.status = 'ACTIVE'
              AND other_enrollment.status = 'ACTIVE'
              AND other_enrollment.activity_id <> target_enrollment.activity_id
            ORDER BY c.name COLLATE NOCASE, other_sport.name COLLATE NOCASE, other_activity.id
            """;

    public List<ConflictCandidate> findActiveActivitiesOfEnrolledStudents(UUID activityClassId) {
        List<ConflictCandidate> candidates = new ArrayList<>();

        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(FIND_ACTIVE_ACTIVITIES_SQL)) {

            statement.setString(1, activityClassId.toString());

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    candidates.add(new ConflictCandidate(
                            UUID.fromString(resultSet.getString("customer_id")),
                            resultSet.getString("customer_name"),
                            UUID.fromString(resultSet.getString("activity_class_id")),
                            resultSet.getString("sport_name"),
                            resultSet.getString("weekdays"),
                            LocalTime.parse(resultSet.getString("start_time")),
                            LocalTime.parse(resultSet.getString("end_time"))
                    ));
                }
            }

            return candidates;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding students affected by activity schedule changes", exception);
        }
    }

    public record ConflictCandidate(
            UUID customerId,
            String customerName,
            UUID activityClassId,
            String sportName,
            String weekdays,
            LocalTime startTime,
            LocalTime endTime
    ) {
    }
}
