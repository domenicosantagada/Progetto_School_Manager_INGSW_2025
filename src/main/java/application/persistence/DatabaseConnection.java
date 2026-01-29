package application.persistence;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String DB_URL = "jdbc:sqlite:gestionale.db"; // URL del database SQLite
    private static DatabaseConnection instance; // Singleton
    private Connection connection; // Connessione al DB

    // Costruttore privato per il pattern Singleton
    private DatabaseConnection() {
        try {
            connection = DriverManager.getConnection(DB_URL); // Crea la connessione
            System.out.println("Connessione al DB riuscita");
        } catch (SQLException e) {
            System.out.println("Connessione al DB fallita: " + e.getMessage());
        }
    }

    // Restituisce l'istanza singleton di DatabaseConnection
    public static synchronized DatabaseConnection getInstance() {
        if (instance == null) {
            instance = new DatabaseConnection();
        }
        return instance;
    }

    // Restituisce la connessione, riaprendo se chiusa
    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                connection = DriverManager.getConnection(DB_URL);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }
}