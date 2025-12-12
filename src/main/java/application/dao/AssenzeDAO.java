package application.dao;

import application.DatabaseConnection;
import application.model.Assenza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AssenzeDAO {

    public AssenzeDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void createTables() {
        String CREATE_ASSENZE_TABLE = """
                CREATE TABLE IF NOT EXISTS assenze (
                    studente TEXT NOT NULL,
                    giorno INTEGER NOT NULL,
                    mese INTEGER NOT NULL,
                    anno INTEGER NOT NULL,
                    motivazione TEXT DEFAULT 'Assenza non giustificata',
                    giustificata BOOLEAN DEFAULT 0,
                    FOREIGN KEY (studente) REFERENCES user(username),
                    PRIMARY KEY (studente, giorno, mese, anno)
                );
                """;
        try (java.sql.Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_ASSENZE_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle Assenze fallita: " + e.getMessage());
        }
    }

    public void addAssenza(Assenza assenza) {
        String query = "INSERT INTO assenze (studente, giorno, mese, anno, motivazione, giustificata) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, assenza.username());
            statement.setInt(2, assenza.giorno());
            statement.setInt(3, assenza.mese());
            statement.setInt(4, assenza.anno());
            statement.setString(5, "Assenza non giustificata");
            statement.setBoolean(6, false);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<Assenza> getAssenzeStudente(String studente) {
        List<Assenza> assenze = new ArrayList<>();
        String query = """
                SELECT giorno, mese, anno, motivazione, giustificata
                FROM assenze
                WHERE studente = ?
                ORDER BY anno DESC, mese DESC, giorno DESC
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int giorno = resultSet.getInt("giorno");
                int mese = resultSet.getInt("mese");
                int anno = resultSet.getInt("anno");
                String motivazione = resultSet.getString("motivazione");
                boolean giustificata = resultSet.getBoolean("giustificata");
                assenze.add(new Assenza(studente, giorno, mese, anno, motivazione, giustificata));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return assenze;
    }

    public List<Assenza> getAssenzeStudente(String studente, int m) {
        List<Assenza> assenze = new ArrayList<>();
        String query = """
                SELECT giorno, mese, anno, motivazione, giustificata
                FROM assenze
                WHERE studente = ? AND mese = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            statement.setInt(2, m);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int giorno = resultSet.getInt("giorno");
                int mese = resultSet.getInt("mese");
                int anno = resultSet.getInt("anno");
                String motivazione = resultSet.getString("motivazione");
                boolean giustificata = resultSet.getBoolean("giustificata");
                assenze.add(new Assenza(studente, giorno, mese, anno, motivazione, giustificata));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return assenze;
    }

    public void justifyAssenza(Assenza assenza, String motivazione) {
        String query = """
                UPDATE assenze
                SET motivazione = ?, giustificata = 1
                WHERE studente = ? AND giorno = ? AND mese = ? AND anno = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, motivazione);
            statement.setString(2, assenza.username());
            statement.setInt(3, assenza.giorno());
            statement.setInt(4, assenza.mese());
            statement.setInt(5, assenza.anno());
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void deleteAssenza(String studente, int giorno, int mese, int anno) {
        String query = "DELETE FROM assenze WHERE studente = ? AND giorno = ? AND mese = ? AND anno = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            statement.setInt(2, giorno);
            statement.setInt(3, mese);
            statement.setInt(4, anno);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'assenza: " + e.getMessage(), e);
        }
    }
}
