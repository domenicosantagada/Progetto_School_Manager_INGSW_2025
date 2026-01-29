package application.dao;

import application.model.TipologiaClasse;
import application.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDAO {

    public SchoolDAO() {
        createTables(); // Crea le tabelle codiciClassi e materie se non esistono
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea le tabelle di supporto per classi e materie
    private void createTables() {
        String CREATE_CODICE_CLASSE_TABLE = """
                CREATE TABLE IF NOT EXISTS codiciClassi (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nomeClasse TEXT NOT NULL,
                    tipologia TEXT,
                    codiceAccesso TEXT NOT NULL
                );
                """;
        String CREATE_MATERIE_TABLE = """
                CREATE TABLE IF NOT EXISTS materie (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    nome TEXT NOT NULL
                );
                """;
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_CODICE_CLASSE_TABLE);
            statement.executeUpdate(CREATE_MATERIE_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle School fallita: " + e.getMessage());
        }
    }

    // Recupera la tipologia e la classe associata a un codice di iscrizione
    public TipologiaClasse getTipologiaUtente(String codiceIscrizione) {
        String query = "SELECT tipologia, nomeClasse FROM codiciClassi WHERE codiceAccesso = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, codiceIscrizione);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return new TipologiaClasse(
                        resultSet.getString("tipologia"),
                        resultSet.getString("nomeClasse")
                );
            }
        } catch (SQLException e) {
            System.out.println("Errore nel recupero tipologia utente: " + e.getMessage());
        }
        return null;
    }

    // Restituisce tutte le materie presenti nell'istituto
    public List<String> getMaterie() {
        List<String> materie = new ArrayList<>();
        String query = "SELECT nome FROM materie";
        try (Statement statement = getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                materie.add(resultSet.getString("nome"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return materie;
    }

    // Controlla se un codice di iscrizione è valido
    public boolean codiceIscrizioneValido(String codice) {
        String query = "SELECT codiceAccesso FROM codiciClassi WHERE codiceAccesso = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, codice);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce la tipologia di utente associata a un codice di iscrizione
    public String tipologiaUser(String codiceIscrizione) {
        String query = "SELECT tipologia FROM codiciClassi WHERE codiceAccesso = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, codiceIscrizione);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("tipologia");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Alias per ottenere tutte le materie
    public List<String> getAllMaterieIstituto() {
        return getMaterie();
    }

    // Restituisce tutti i nomi delle classi per studenti
    public List<String> getAllClassiNames() {
        List<String> classi = new ArrayList<>();
        String query = "SELECT DISTINCT nomeClasse FROM codiciClassi WHERE tipologia = 'studente' ORDER BY nomeClasse";
        try (Statement statement = getConnection().createStatement();
             ResultSet resultSet = statement.executeQuery(query)) {
            while (resultSet.next()) {
                classi.add(resultSet.getString("nomeClasse"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return classi;
    }
}