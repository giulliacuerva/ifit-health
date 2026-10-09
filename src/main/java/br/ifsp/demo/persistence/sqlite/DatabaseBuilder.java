package br.ifsp.demo.persistence.sqlite;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.UUID;

public class DatabaseBuilder {

    private static final String DATABASE_NAME = "database.db";
    private static final String DATABASE_URL = "jdbc:sqlite:" + DATABASE_NAME;

    public static void main(String[] args) {
        new DatabaseBuilder().build();
    }

    public void build() {
        try {
            dropDatabaseIfExists();

            try (Connection connection = DriverManager.getConnection(DATABASE_URL)) {
                enableForeignKeys(connection);
                connection.setAutoCommit(false);

                try {
                    createCustomersTable(connection);
                    createTrainerTable(connection);
                    createSportsTable(connection);
                    createRoomsTable(connection);

                    createActivityClassTable(connection);
                    createActivityPricesTable(connection);
                    createEnrollmentsTable(connection);
                    createEnrollmentActivitiesTable(connection);

                    insertCustomers(connection);
                    insertTrainers(connection);
                    insertSports(connection);
                    insertRooms(connection);

                    connection.commit();

                    System.out.println("❤ All set ❤");
                } catch (SQLException e) {
                    connection.rollback();
                    throw e;
                }
            }

        } catch (IOException | SQLException e) {
            System.err.println("Ops! Something went wrong.");
            e.printStackTrace();
        }
    }

    private void dropDatabaseIfExists() throws IOException {
        final Path path = Paths.get(DATABASE_NAME);

        if (Files.exists(path)) {
            Files.delete(path);
            System.out.println("Removing existing database...");
        }
    }

    private void enableForeignKeys(Connection connection) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }
    }

    private void createCustomersTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS customers (
                    id TEXT PRIMARY KEY,
                    user_id TEXT,
                    cpf TEXT NOT NULL UNIQUE,
                    name TEXT NOT NULL,
                    birthdate TEXT NOT NULL,
                    gender TEXT NOT NULL,
                    tel TEXT,
                    email TEXT NOT NULL,
                    address_cep TEXT,
                    address_number TEXT,
                    address_street TEXT,
                    address_city TEXT,
                    address_state TEXT,
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """;

        execute(connection, sql);
    }

    private void createTrainerTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS trainer (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL,
                    email TEXT NOT NULL,
                    birthdate TEXT NOT NULL,
                    address_cep TEXT,
                    address_number TEXT,
                    address_street TEXT,
                    address_city TEXT,
                    address_state TEXT,
                    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1))
                );
                """;

        execute(connection, sql);
    }

    private void createSportsTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS sports (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    description TEXT,
                    room_type TEXT NOT NULL,
                    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP
                );
                """;

        execute(connection, sql);
    }

    private void createRoomsTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS rooms (
                    id TEXT PRIMARY KEY,
                    name TEXT NOT NULL UNIQUE,
                    room_type TEXT NOT NULL,
                    capacity INTEGER NOT NULL CHECK (capacity > 0)
                );
                """;

        execute(connection, sql);
    }

    private void createActivityClassTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS activity_class (
                    id TEXT PRIMARY KEY,
                    sport_id TEXT NOT NULL,
                    room_id TEXT NOT NULL,
                    coach_id TEXT NOT NULL,
                    name TEXT NOT NULL,
                    weekday TEXT NOT NULL,
                    start_time TEXT NOT NULL,
                    end_time TEXT NOT NULL,
                    capacity INTEGER NOT NULL CHECK (capacity > 0),
                    monthly_fee NUMERIC NOT NULL CHECK (monthly_fee >= 0),
                    active INTEGER NOT NULL DEFAULT 1 CHECK (active IN (0, 1)),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    UNIQUE (sport_id, name),
                    FOREIGN KEY (sport_id) REFERENCES sports(id),
                    FOREIGN KEY (room_id) REFERENCES rooms(id),
                    FOREIGN KEY (coach_id) REFERENCES trainer(id)
                );
                """;

        execute(connection, sql);
    }

    private void createActivityPricesTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS activity_prices (
                    id TEXT PRIMARY KEY,
                    activity_id TEXT NOT NULL,
                    monthly_fee NUMERIC NOT NULL CHECK (monthly_fee >= 0),
                    valid_from TEXT NOT NULL,
                    UNIQUE (activity_id, valid_from),
                    FOREIGN KEY (activity_id) REFERENCES activity_class(id)
                );
                """;

        execute(connection, sql);
    }

    private void createEnrollmentsTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS enrollments (
                    id TEXT PRIMARY KEY,
                    customer_id TEXT NOT NULL UNIQUE,
                    enroll_date TEXT NOT NULL,
                    end_date TEXT,
                    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
                    created_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    updated_at TEXT NOT NULL DEFAULT CURRENT_TIMESTAMP,
                    FOREIGN KEY (customer_id) REFERENCES customers(id)
                );
                """;

        execute(connection, sql);
    }

    private void createEnrollmentActivitiesTable(Connection connection) throws SQLException {
        final String sql = """
                CREATE TABLE IF NOT EXISTS enrollment_activities (
                    id TEXT PRIMARY KEY,
                    enrollment_id TEXT NOT NULL,
                    activity_id TEXT NOT NULL,
                    monthly_fee NUMERIC NOT NULL CHECK (monthly_fee >= 0),
                    start_date TEXT NOT NULL,
                    end_date TEXT,
                    status TEXT NOT NULL DEFAULT 'ACTIVE' CHECK (status IN ('ACTIVE', 'INACTIVE')),
                    FOREIGN KEY (enrollment_id) REFERENCES enrollments(id),
                    FOREIGN KEY (activity_id) REFERENCES activity_class(id)
                );
                """;

        execute(connection, sql);
    }

    private void insertCustomers(Connection connection) throws SQLException {
        final String sql = """
                INSERT INTO customers (
                    id,
                    user_id,
                    cpf,
                    name,
                    birthdate,
                    gender,
                    tel,
                    email,
                    address_cep,
                    address_number,
                    address_street,
                    address_city,
                    address_state
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            UUID customerId = UUID.randomUUID();

            stmt.setString(1, customerId.toString());
            stmt.setNull(2, java.sql.Types.VARCHAR);
            stmt.setString(3, "12345678901");
            stmt.setString(4, "Ana Souza");
            stmt.setString(5, "1998-05-12");
            stmt.setString(6, "FEMALE");
            stmt.setString(7, "16999990001");
            stmt.setString(8, "ana.souza@example.com");
            stmt.setString(9, "13560-001");
            stmt.setString(10, "100");
            stmt.setString(11, "Rua das Flores");
            stmt.setString(12, "São Carlos");
            stmt.setString(13, "SP");
            stmt.executeUpdate();

            customerId = UUID.randomUUID();

            stmt.setString(1, customerId.toString());
            stmt.setNull(2, java.sql.Types.VARCHAR);
            stmt.setString(3, "23456789012");
            stmt.setString(4, "Bruno Lima");
            stmt.setString(5, "1995-09-23");
            stmt.setString(6, "MALE");
            stmt.setString(7, "16999990002");
            stmt.setString(8, "bruno.lima@example.com");
            stmt.setString(9, "13560-002");
            stmt.setString(10, "250");
            stmt.setString(11, "Avenida Central");
            stmt.setString(12, "São Carlos");
            stmt.setString(13, "SP");
            stmt.executeUpdate();

            customerId = UUID.randomUUID();

            stmt.setString(1, customerId.toString());
            stmt.setNull(2, java.sql.Types.VARCHAR);
            stmt.setString(3, "34567890123");
            stmt.setString(4, "Carla Mendes");
            stmt.setString(5, "2000-02-10");
            stmt.setString(6, "FEMALE");
            stmt.setString(7, "16999990003");
            stmt.setString(8, "carla.mendes@example.com");
            stmt.setString(9, "13560-003");
            stmt.setString(10, "45");
            stmt.setString(11, "Rua das Palmeiras");
            stmt.setString(12, "São Carlos");
            stmt.setString(13, "SP");
            stmt.executeUpdate();
        }
    }

    private void insertTrainers(Connection connection) throws SQLException {
        final String sql = """
                INSERT INTO trainer (
                    id,
                    name,
                    email,
                    birthdate,
                    address_cep,
                    address_number,
                    address_street,
                    address_city,
                    address_state,
                    active
                )
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);
                """;

        String[][] trainers = {
                {
                        "John Doe",
                        "john.doe@example.com",
                        "1985-03-15",
                        "13560-101",
                        "10",
                        "Rua A",
                        "São Carlos",
                        "SP"
                },
                {
                        "Maria Silva",
                        "maria.silva@example.com",
                        "1990-07-20",
                        "13560-102",
                        "20",
                        "Rua B",
                        "São Carlos",
                        "SP"
                },
                {
                        "Carlos Oliveira",
                        "carlos.oliveira@example.com",
                        "1988-11-02",
                        "13560-103",
                        "30",
                        "Rua C",
                        "São Carlos",
                        "SP"
                },
                {
                        "Ana Martins",
                        "ana.martins@example.com",
                        "1987-04-12",
                        "13560-104",
                        "45",
                        "Rua das Acácias",
                        "São Carlos",
                        "SP"
                },
                {
                        "Pedro Santos",
                        "pedro.santos@example.com",
                        "1992-08-25",
                        "13560-105",
                        "120",
                        "Rua das Palmeiras",
                        "São Carlos",
                        "SP"
                },
                {
                        "Juliana Costa",
                        "juliana.costa@example.com",
                        "1989-01-17",
                        "13560-106",
                        "300",
                        "Avenida Central",
                        "São Carlos",
                        "SP"
                },
                {
                        "Rafael Almeida",
                        "rafael.almeida@example.com",
                        "1984-06-08",
                        "13560-107",
                        "75",
                        "Rua dos Esportes",
                        "São Carlos",
                        "SP"
                },
                {
                        "Fernanda Rocha",
                        "fernanda.rocha@example.com",
                        "1993-11-30",
                        "13560-108",
                        "200",
                        "Rua São Paulo",
                        "São Carlos",
                        "SP"
                }
        };

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            for (String[] trainer : trainers) {
                stmt.setString(1, UUID.randomUUID().toString());
                stmt.setString(2, trainer[0]);
                stmt.setString(3, trainer[1]);
                stmt.setString(4, trainer[2]);
                stmt.setString(5, trainer[3]);
                stmt.setString(6, trainer[4]);
                stmt.setString(7, trainer[5]);
                stmt.setString(8, trainer[6]);
                stmt.setString(9, trainer[7]);
                stmt.setInt(10, 1);
                stmt.executeUpdate();
            }
        }
    }

    private void insertSports(Connection connection) throws SQLException {
        final String sql = """
                INSERT INTO sports (
                    id,
                    name,
                    description,
                    room_type,
                    active
                )
                VALUES (?, ?, ?, ?, ?);
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            UUID sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Musculação");
            stmt.setString(3, "Treinamento de força com equipamentos");
            stmt.setString(4, "GYM");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Treinamento Funcional");
            stmt.setString(3, "Exercícios funcionais em grupo");
            stmt.setString(4, "GYM");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Natação");
            stmt.setString(3, "Aulas e treinamentos de natação");
            stmt.setString(4, "POOL");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Judô");
            stmt.setString(3, "Aulas e treinamentos de judô");
            stmt.setString(4, "TATAMI");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Futebol");
            stmt.setString(3, "Aulas e treinamentos de futebol");
            stmt.setString(4, "COURT");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Vôlei");
            stmt.setString(3, "Aulas e treinamentos de vôlei");
            stmt.setString(4, "COURT");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Tênis de Mesa");
            stmt.setString(3, "Aulas e treinamentos de tênis de mesa");
            stmt.setString(4, "TABLE_TENNIS");
            stmt.setInt(5, 1);
            stmt.executeUpdate();

            sportId = UUID.randomUUID();

            stmt.setString(1, sportId.toString());
            stmt.setString(2, "Atletismo");
            stmt.setString(3, "Treinamentos de corrida e modalidades de atletismo");
            stmt.setString(4, "TRACK");
            stmt.setInt(5, 1);
            stmt.executeUpdate();
        }
    }

    private void insertRooms(Connection connection) throws SQLException {
        final String sql = """
                INSERT INTO rooms (
                    id,
                    name,
                    room_type,
                    capacity
                )
                VALUES (?, ?, ?, ?);
                """;

        try (PreparedStatement stmt = connection.prepareStatement(sql)) {
            UUID roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Sala de Musculação");
            stmt.setString(3, "GYM");
            stmt.setInt(4, 30);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Sala de Treinamento Funcional");
            stmt.setString(3, "GYM");
            stmt.setInt(4, 20);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Piscina");
            stmt.setString(3, "POOL");
            stmt.setInt(4, 20);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Sala de Judô");
            stmt.setString(3, "TATAMI");
            stmt.setInt(4, 25);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Quadra de Futebol");
            stmt.setString(3, "COURT");
            stmt.setInt(4, 30);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Quadra de Vôlei");
            stmt.setString(3, "COURT");
            stmt.setInt(4, 18);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Sala de Tênis de Mesa");
            stmt.setString(3, "TABLE_TENNIS");
            stmt.setInt(4, 16);
            stmt.executeUpdate();

            roomId = UUID.randomUUID();

            stmt.setString(1, roomId.toString());
            stmt.setString(2, "Pista de Atletismo");
            stmt.setString(3, "TRACK");
            stmt.setInt(4, 40);
            stmt.executeUpdate();
        }
    }

    private void execute(Connection connection, String sql) throws SQLException {
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        }
    }
}