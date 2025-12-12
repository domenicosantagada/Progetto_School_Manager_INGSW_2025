package application.dao;

import application.DatabaseConnection;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class VotiDAO {

    public VotiDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void createTables() {
        String CREATE_STUDENTI_VOTI_TABLE = """
                CREATE TABLE IF NOT EXISTS studentiVoti (
                    studente TEXT NOT NULL,
                    materia TEXT NOT NULL,
                    dataValutazione TEXT NOT NULL,
                    voto INTEGER NOT NULL,
                    PRIMARY KEY (studente, materia),
                    FOREIGN KEY (studente) REFERENCES user(username),
                    FOREIGN KEY (materia) REFERENCES materie(nome)
                );
                """;
        try (java.sql.Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_STUDENTI_VOTI_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle Voti fallita: " + e.getMessage());
        }
    }

    // New method to initialize votes for a student in a materia
    public void initializeVoto(String studente, String materia) {
        String query = "INSERT INTO studentiVoti (studente, materia, dataValutazione, voto) VALUES (?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            statement.setString(2, materia);
            statement.setString(3, "Nd");
            statement.setInt(4, 0);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<StudenteTable> getStudentiClasse(String classe, String materia) {
        List<StudenteTable> studenti = new ArrayList<>();
        String query = """
                SELECT u.nome, u.cognome, sv.dataValutazione, sv.voto, u.username
                FROM user u
                JOIN studentiVoti sv ON u.username = sv.studente
                WHERE u.classeAppartenenza = ? AND sv.materia = ?
                ORDER BY u.cognome, u.nome, sv.dataValutazione
                """;

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, classe);
            statement.setString(2, materia);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String cognome = resultSet.getString("cognome");
                String dataValutazione = resultSet.getString("dataValutazione");
                Integer voto = resultSet.getInt("voto");
                String username = resultSet.getString("username");
                studenti.add(new StudenteTable(nome, cognome, dataValutazione, voto, username));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli studenti: " + e.getMessage(), e);
        }
        return studenti;
    }

    public boolean updateVoto(ValutazioneStudente valutazione) {
        String query = """
                UPDATE studentiVoti
                SET voto = ?, dataValutazione = ?
                WHERE studente = ? AND materia = ?
                """;
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, valutazione.voto());
            statement.setString(2, valutazione.data());
            statement.setString(3, valutazione.studente());
            statement.setString(4, valutazione.materia());
            statement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<ValutazioneStudente> getVotiStudente(String studente) {
        List<ValutazioneStudente> voti = new ArrayList<>();
        String query = """
                SELECT sv.materia, sv.dataValutazione, sv.voto, pm.username AS professore
                FROM studentiVoti sv
                JOIN profMateria pm ON sv.materia = pm.materia
                WHERE sv.studente = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String materia = resultSet.getString("materia");
                String dataValutazione = resultSet.getString("dataValutazione");
                int voto = resultSet.getInt("voto");
                String professore = resultSet.getString("professore");
                voti.add(new ValutazioneStudente(studente, professore, materia, dataValutazione, voto));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return voti;
    }
}
