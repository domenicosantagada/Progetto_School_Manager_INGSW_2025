package application.dao;

import application.DatabaseConnection;
import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompitiDAO {

    public CompitiDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    private void createTables() {
        String CREATE_COMPITI_TABLE = """
                CREATE TABLE IF NOT EXISTS compiti (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    professore TEXT NOT NULL,
                    materia TEXT NOT NULL,
                    data TEXT NOT NULL,
                    descrizione TEXT NOT NULL,
                    classe TEXT NOT NULL,
                    FOREIGN KEY (professore) REFERENCES user(username),
                    FOREIGN KEY (materia) REFERENCES materie(nome)
                );
                """;
        String CREATE_ELABORATI_TABLE = """
                CREATE TABLE IF NOT EXISTS elaboratiCaricati (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    compitoId INTEGER NOT NULL,
                    studente TEXT NOT NULL,
                    data TEXT NOT NULL,
                    commento TEXT,
                    file BLOB,
                    FOREIGN KEY (compitoId) REFERENCES compiti(id),
                    FOREIGN KEY (studente) REFERENCES user(username)
                );
                """;
        try (java.sql.Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_COMPITI_TABLE);
            statement.executeUpdate(CREATE_ELABORATI_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle Compiti fallita: " + e.getMessage());
        }
    }

    public boolean insertCompito(CompitoAssegnato compito) {
        String query = "INSERT INTO compiti (professore, materia, data, descrizione, classe) VALUES (?, ?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, compito.prof());
            statement.setString(2, compito.materia());
            statement.setString(3, compito.data());
            statement.setString(4, compito.descrizione());
            statement.setString(5, compito.classe());
            statement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<CompitoAssegnato> getCompitiClasse(String classe) {
        List<CompitoAssegnato> compiti = new ArrayList<>();
        String query = """
                SELECT id, professore, materia, data, descrizione, classe
                FROM compiti
                WHERE classe = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, classe);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int id = resultSet.getInt("id");
                String professore = resultSet.getString("professore");
                String materia = resultSet.getString("materia");
                String data = resultSet.getString("data");
                String descrizione = resultSet.getString("descrizione");
                String classeCompito = resultSet.getString("classe");
                compiti.add(new CompitoAssegnato(id, professore, materia, data, descrizione, classeCompito));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return compiti;
    }

    public boolean insertElaborato(ElaboratoCaricato elaborato) {
        String query = "INSERT INTO elaboratiCaricati (compitoId, studente, data, commento, file) VALUES (?, ?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaborato.compito().id());
            statement.setString(2, elaborato.studente());
            statement.setString(3, elaborato.data());
            statement.setString(4, elaborato.commento());
            statement.setBytes(5, elaborato.file());
            statement.executeUpdate();
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public List<ElaboratoCaricato> getElaboratiCompito(int compitoId) {
        List<ElaboratoCaricato> elaborati = new ArrayList<>();
        String query = """
                SELECT ec.id, ec.studente, ec.data, ec.commento, ec.file,
                       c.id as compitoId, c.professore, c.materia, c.data as dataCompito, c.descrizione, c.classe
                FROM elaboratiCaricati ec
                JOIN compiti c ON ec.compitoId = c.id
                WHERE ec.compitoId = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                CompitoAssegnato compito = new CompitoAssegnato(
                        resultSet.getInt("compitoId"),
                        resultSet.getString("professore"),
                        resultSet.getString("materia"),
                        resultSet.getString("dataCompito"),
                        resultSet.getString("descrizione"),
                        resultSet.getString("classe")
                );

                int id = resultSet.getInt("id");
                String studente = resultSet.getString("studente");
                String data = resultSet.getString("data");
                String commento = resultSet.getString("commento");
                byte[] file = resultSet.getBytes("file");

                elaborati.add(new ElaboratoCaricato(compito, studente, data, commento, file, id));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return elaborati;
    }

    public boolean hasElaboratiForCompito(int compitoId) {
        String query = "SELECT COUNT(*) FROM elaboratiCaricati WHERE compitoId = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo degli elaborati: " + e.getMessage(), e);
        }
        return false;
    }

    public boolean deleteCompito(int compitoId) {
        String query = "DELETE FROM compiti WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del compito: " + e.getMessage(), e);
        }
    }

    public boolean deleteElaborato(int elaboratoId) {
        String query = "DELETE FROM elaboratiCaricati WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaboratoId);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'elaborato: " + e.getMessage(), e);
        }
    }
}
