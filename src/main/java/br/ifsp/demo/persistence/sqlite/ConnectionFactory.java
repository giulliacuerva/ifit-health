package br.ifsp.demo.persistence.sqlite;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final String URL = "jdbc:sqlite:database.db";

    private ConnectionFactory() {
    }

    public static Connection createConnection() throws SQLException {
        Connection connection = DriverManager.getConnection(URL);

        try (var stmt = connection.createStatement()) {
            stmt.execute("PRAGMA foreign_keys = ON");
        }

        return connection;
    }
}