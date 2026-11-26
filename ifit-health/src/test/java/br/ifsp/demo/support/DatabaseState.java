package br.ifsp.demo.support;

import br.ifsp.demo.persistence.sqlite.ConnectionFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public final class DatabaseState {
    private static final Path DATABASE = Path.of("database.db");
    private static Path snapshot;

    private DatabaseState() {
    }

    public static synchronized void snapshotOnce() {
        if (snapshot != null) {
            return;
        }
        try {
            snapshot = Files.createTempFile("ifit-health-snapshot-", ".db");
            snapshot.toFile().deleteOnExit();
            Files.copy(DATABASE, snapshot, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new UncheckedIOException("Não foi possível copiar o banco " + DATABASE.toAbsolutePath(), exception);
        }
    }

    public static synchronized void restore() {
        snapshotOnce();
        try (Connection connection = ConnectionFactory.createConnection();
             Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = OFF");
            statement.execute("ATTACH DATABASE '" + snapshot.toAbsolutePath().toString().replace("'", "''") + "' AS snapshot");
            try {
                statement.execute("BEGIN");
                for (String table : tables(statement)) {
                    statement.executeUpdate("DELETE FROM main.\"" + table + "\"");
                    statement.executeUpdate("INSERT INTO main.\"" + table + "\" SELECT * FROM snapshot.\"" + table + "\"");
                }
                statement.execute("COMMIT");
            } catch (SQLException exception) {
                statement.execute("ROLLBACK");
                throw exception;
            } finally {
                statement.execute("DETACH DATABASE snapshot");
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("Não foi possível restaurar o banco de teste", exception);
        }
    }

    private static List<String> tables(Statement statement) throws SQLException {
        List<String> tables = new ArrayList<>();
        try (ResultSet resultSet = statement.executeQuery(
                "SELECT name FROM snapshot.sqlite_master WHERE type = 'table' AND name NOT LIKE 'sqlite_%'")) {
            while (resultSet.next()) {
                tables.add(resultSet.getString("name"));
            }
        }
        return tables;
    }
}
