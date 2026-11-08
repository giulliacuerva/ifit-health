package br.ifsp.demo.persistence.sqlite;

import org.sqlite.SQLiteConfig;

import java.sql.Connection;
import java.sql.SQLException;

public final class ConnectionFactory {

    private static final String URL = "jdbc:sqlite:database.db";
    private static final int BUSY_TIMEOUT_MILLIS = 5_000;

    private ConnectionFactory() {
    }

    public static Connection createConnection() throws SQLException {
        return createConnection(SQLiteConfig.TransactionMode.DEFERRED);
    }

    public static Connection createImmediateTransactionConnection() throws SQLException {
        return createConnection(SQLiteConfig.TransactionMode.IMMEDIATE);
    }

    private static Connection createConnection(SQLiteConfig.TransactionMode transactionMode)
            throws SQLException {
        SQLiteConfig config = new SQLiteConfig();
        config.setBusyTimeout(BUSY_TIMEOUT_MILLIS);
        config.enforceForeignKeys(true);
        config.setTransactionMode(transactionMode);
        return config.createConnection(URL);
    }
}
