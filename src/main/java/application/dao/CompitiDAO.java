package application.dao;

import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import application.persistence.DatabaseConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CompitiDAO {

    public CompitiDAO() {
        createTables(); // Crea le tabelle compiti ed elaborati se non esistono
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea le tabelle per compiti ed elaborati
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

    // Inserisce un nuovo compito nella tabella
    public boolean insertCompito(CompitoAssegnato compito) {
        String query = "INSERT INTO compiti (professore, materia, data, descrizione, classe) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, compito.prof());
            statement.setString(2, compito.materia());
            statement.setString(3, compito.data());
            statement.setString(4, compito.descrizione());
            statement.setString(5, compito.classe());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce tutti i compiti assegnati a una classe
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
                compiti.add(new CompitoAssegnato(
                        resultSet.getInt("id"),
                        resultSet.getString("professore"),
                        resultSet.getString("materia"),
                        resultSet.getString("data"),
                        resultSet.getString("descrizione"),
                        resultSet.getString("classe")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return compiti;
    }

    // Inserisce un elaborato per un compito
    public boolean insertElaborato(ElaboratoCaricato elaborato) {
        String query = "INSERT INTO elaboratiCaricati (compitoId, studente, data, commento, file) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaborato.compito().id());
            statement.setString(2, elaborato.studente());
            statement.setString(3, elaborato.data());
            statement.setString(4, elaborato.commento());
            statement.setBytes(5, elaborato.file());
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Restituisce tutti gli elaborati di un compito
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

                elaborati.add(new ElaboratoCaricato(
                        compito,
                        resultSet.getString("studente"),
                        resultSet.getString("data"),
                        resultSet.getString("commento"),
                        resultSet.getBytes("file"),
                        resultSet.getInt("id")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return elaborati;
    }

    // Controlla se un compito ha almeno un elaborato
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

    // Elimina un compito dalla tabella
    public boolean deleteCompito(int compitoId) {
        String query = "DELETE FROM compiti WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione del compito: " + e.getMessage(), e);
        }
    }

    // Elimina un elaborato specifico
    public boolean deleteElaborato(int elaboratoId) {
        String query = "DELETE FROM elaboratiCaricati WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaboratoId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'elaborato: " + e.getMessage(), e);
        }
    }

    // Elimiina tutti i compiti di una materia specifica
    public boolean deleteCompitiByMateria(String materia) {
        String query = "DELETE FROM compiti WHERE materia = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, materia);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dei compiti per materia: " + e.getMessage(), e);
        }
    }

    // Elimina tutti gli elaborati di uno studente specifico
    public boolean deleteElaboratiByStudente(String studente) {
        String query = "DELETE FROM elaboratiCaricati WHERE studente = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione degli elaborati per studente: " + e.getMessage(), e);
        }
    }

    // Elimina tutti i compiti di una classe specifica
    public boolean deleteCompitiByClasse(String classe) {
        String query = "DELETE FROM compiti WHERE classe = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, classe);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dei compiti per classe: " + e.getMessage(), e);
        }
    }


    // Elimina di tutti gli elaborati caricati nell'istituto
    public boolean deleteAllElaborati() {
        String query = "DELETE FROM elaboratiCaricati";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione di tutti gli elaborati: " + e.getMessage(), e);
        }
    }
}