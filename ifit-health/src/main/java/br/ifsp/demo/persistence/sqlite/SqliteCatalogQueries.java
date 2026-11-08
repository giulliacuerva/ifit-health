package br.ifsp.demo.persistence.sqlite;

import br.ifsp.demo.domain.model.enums.RoomType;
import org.springframework.stereotype.Repository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Repository
public class SqliteCatalogQueries {

    public List<SportRow> findSports() {
        String sql = """
                SELECT id, name, description, room_type, active
                FROM sports
                ORDER BY name COLLATE NOCASE, id
                """;

        List<SportRow> sports = new ArrayList<>();
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                sports.add(new SportRow(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        RoomType.valueOf(resultSet.getString("room_type")),
                        resultSet.getInt("active") == 1
                ));
            }
            return sports;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding sports catalog", exception);
        }
    }

    public List<RoomRow> findRooms() {
        String sql = """
                SELECT id, name, room_type, capacity
                FROM rooms
                ORDER BY name COLLATE NOCASE, id
                """;

        List<RoomRow> rooms = new ArrayList<>();
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                rooms.add(new RoomRow(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("name"),
                        RoomType.valueOf(resultSet.getString("room_type")),
                        resultSet.getInt("capacity")
                ));
            }
            return rooms;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding rooms catalog", exception);
        }
    }

    public List<TrainerRow> findTrainers() {
        String sql = """
                SELECT id, name, active
                FROM trainer
                ORDER BY name COLLATE NOCASE, id
                """;

        List<TrainerRow> trainers = new ArrayList<>();
        try (Connection connection = ConnectionFactory.createConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {
                trainers.add(new TrainerRow(
                        UUID.fromString(resultSet.getString("id")),
                        resultSet.getString("name"),
                        resultSet.getInt("active") == 1
                ));
            }
            return trainers;
        } catch (SQLException exception) {
            throw new RuntimeException("Error finding trainers catalog", exception);
        }
    }

    public record SportRow(UUID id, String name, String description, RoomType roomType, boolean active) {
    }

    public record RoomRow(UUID id, String name, RoomType roomType, int capacity) {
    }

    public record TrainerRow(UUID id, String name, boolean active) {
    }
}
