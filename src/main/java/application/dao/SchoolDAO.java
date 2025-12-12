package application.dao;

import application.DatabaseConnection;
import application.model.TipologiaClasse;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SchoolDAO {

    public SchoolDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

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

    public TipologiaClasse getTipologiaUtente(String codiceIscrizione) {
        String query = "SELECT tipologia, nomeClasse FROM codiciClassi WHERE codiceAccesso = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, codiceIscrizione);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String tipologia = resultSet.getString("tipologia");
                String classe = resultSet.getString("nomeClasse");
                return new TipologiaClasse(tipologia, classe);
            }
        } catch (SQLException e) {
            System.out.println("Errore: " + e.getMessage());
            return null;
        }
        return null;
    }

    public List<String> getMaterie() {
        String query = "SELECT nome FROM materie";
        List<String> materie = new ArrayList<>();
        try (Statement statement = getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                materie.add(resultSet.getString("nome"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return materie;
    }

    public boolean codiceIscrizioneValido(String newValue) {
        String query = "SELECT codiceAccesso FROM codiciClassi WHERE codiceAccesso = ?";
        boolean valido = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, newValue);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                valido = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return valido;
    }

    public String tipologiaUser(String codiceIscrizione) {
        String query = "SELECT tipologia FROM codiciClassi WHERE codiceAccesso = ?";
        String result = "";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, codiceIscrizione);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("tipologia");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<String> getAllMaterieIstituto() {
        return getMaterie();
    }

    public List<String> getAllClassiNames() {
        String query = "SELECT DISTINCT nomeClasse FROM codiciClassi WHERE tipologia = 'studente' ORDER BY nomeClasse";
        List<String> classi = new ArrayList<>();
        try (Statement statement = getConnection().createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                classi.add(resultSet.getString("nomeClasse"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return classi;
    }
}
