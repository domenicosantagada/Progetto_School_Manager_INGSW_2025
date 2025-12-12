package application.dao;

import application.DatabaseConnection;
import application.model.Nota;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class NoteDAO {

    public NoteDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

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

    public boolean insertNota(Nota nota) {
        String query = "INSERT INTO note (studente, professore, nota, dataInserimento) VALUES (?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, nota.studente());
            statement.setString(2, nota.prof());
            statement.setString(3, nota.nota());
            statement.setString(4, nota.data());
            statement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<Nota> getNoteStudente(String studente) {
        List<Nota> note = new ArrayList<>();
        String query = "SELECT professore, nota, dataInserimento FROM note WHERE studente = ?";

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String professore = resultSet.getString("professore");
                String nota = resultSet.getString("nota");
                String dataInserimento = resultSet.getString("dataInserimento");
                note.add(new Nota(studente, professore, nota, dataInserimento));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return note;
    }
}
