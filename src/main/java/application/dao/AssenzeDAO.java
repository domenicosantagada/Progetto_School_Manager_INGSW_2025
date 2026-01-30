package application.dao;

import application.model.Assenza;
import application.persistence.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


// AssenzeDAO gestisce le operazioni CRUD per le assenze degli studenti
// ovvero creare, leggere, aggiornare ed eliminare assenze
// Dao sta per Data Access Object
public class AssenzeDAO {

    public AssenzeDAO() {
        createTables(); // Crea la tabella assenze se non esiste
    }

    // Ottiene la connessione al database
    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea la tabella assenze con vincoli e chiave primaria
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

    // Aggiunge una nuova assenza per uno studente
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

    // Restituisce tutte le assenze di uno studente (dato il suo username) ordinate per data decrescente
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

    // Restituisce le assenze di uno studente per un mese specifico
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

    // Giustifica un'assenza aggiornando motivazione e flag
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

    // Elimina un'assenza specifica
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

    // elimina tutte le assenze di uno studente
    public void deleteAssenzeStudente(String studente) {
        String query = "DELETE FROM assenze WHERE studente = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione delle assenze dello studente: " + e.getMessage(), e);
        }
    }

    // Giustifica tutte le assenze di uno studente
    public void justifyAllAssenzeStudente(String studente, String motivazione) {
        String query = """
                UPDATE assenze
                SET motivazione = ?, giustificata = 1
                WHERE studente = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, motivazione);
            statement.setString(2, studente);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Giustifica tutte le assenze degli studenti di una classe specifica
    public void justifyAllAssenzeClasse(String classe, String motivazione) {
        String query = """
                UPDATE assenze
                SET motivazione = ?, giustificata = 1
                WHERE studente IN (
                    SELECT username FROM user WHERE classe = ?
                )
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, motivazione);
            statement.setString(2, classe);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Giustifica tutte le assenze di tutti gli studenti
    public void justifyAllAssenze(String motivazione) {
        String query = """
                UPDATE assenze
                SET motivazione = ?, giustificata = 1
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, motivazione);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}