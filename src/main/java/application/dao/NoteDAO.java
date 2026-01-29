package application.dao;

import application.model.Nota;
import application.persistence.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public NoteDAO() {
        createTables(); // Crea la tabella note se non esiste
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea la tabella delle note disciplinari
    private void createTables() {
        String CREATE_NOTE_TABLE = """
                CREATE TABLE IF NOT EXISTS note (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    studente TEXT NOT NULL,
                    professore TEXT NOT NULL,
                    nota TEXT NOT NULL,
                    dataInserimento TEXT NOT NULL,
                    FOREIGN KEY (studente) REFERENCES user(username),
                    FOREIGN KEY (professore) REFERENCES user(username)
                );
                """;
        try (java.sql.Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_NOTE_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle Note fallita: " + e.getMessage());
        }
    }

    // Inserisce una nuova nota per uno studente
    public boolean insertNota(Nota nota) {
        String query = "INSERT INTO note (studente, professore, nota, dataInserimento) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, nota.studente());
            statement.setString(2, nota.prof());
            statement.setString(3, nota.nota());
            statement.setString(4, nota.data());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce tutte le note di uno studente
    public List<Nota> getNoteStudente(String studente) {
        List<Nota> note = new ArrayList<>();
        String query = "SELECT professore, nota, dataInserimento FROM note WHERE studente = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                note.add(new Nota(
                        studente,
                        resultSet.getString("professore"),
                        resultSet.getString("nota"),
                        resultSet.getString("dataInserimento")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return note;
    }
}