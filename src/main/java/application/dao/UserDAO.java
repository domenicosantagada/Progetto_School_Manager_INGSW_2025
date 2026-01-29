package application.dao;

import application.model.Professore;
import application.model.Studente;
import application.persistence.DatabaseConnection;
import application.utility.BCryptService;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public UserDAO() {
        createTables(); // Crea le tabelle user e profMateria se non esistono
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea le tabelle utenti e associazioni professore-materia
    private void createTables() {

        String CREATE_USER_TABLE = """ 
                CREATE TABLE IF NOT EXISTS user (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL UNIQUE,
                    nome TEXT NOT NULL,
                    cognome TEXT NOT NULL,
                    password TEXT NOT NULL,
                    dataNascita TEXT NOT NULL,
                    classeAppartenenza TEXT NOT NULL,
                    tipo TEXT NOT NULL
                );
                """;

        String CREATE_PROF_MATERIA_TABLE = """ 
                CREATE TABLE IF NOT EXISTS profMateria (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    username TEXT NOT NULL,
                    materia TEXT NOT NULL,
                    FOREIGN KEY (username) REFERENCES user(username)
                );
                """;

        try (java.sql.Statement statement = getConnection().createStatement()) {

            statement.executeUpdate(CREATE_USER_TABLE);

            statement.executeUpdate(CREATE_PROF_MATERIA_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle User fallita: " + e.getMessage());
        }
    }

    // Inserisce un nuovo studente
    public boolean insertStudente(Studente studente) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente.user().username());
            statement.setString(2, studente.user().nome());
            statement.setString(3, studente.user().cognome());
            statement.setString(4, studente.user().password());
            statement.setString(5, studente.user().data());
            statement.setString(6, studente.classe());
            statement.setString(7, "studente");
            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Inserisce un nuovo professore e la sua materia
    public boolean insertProfessore(Professore professore) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query2 = "INSERT INTO profMateria (username, materia) VALUES (?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query);
             PreparedStatement statement2 = getConnection().prepareStatement(query2)) {
            statement.setString(1, professore.user().username());
            statement.setString(2, professore.user().nome());
            statement.setString(3, professore.user().cognome());
            statement.setString(4, professore.user().password());
            statement.setString(5, professore.user().data());
            statement.setString(6, professore.classe());
            statement.setString(7, "professore");
            statement.executeUpdate();

            statement2.setString(1, professore.user().username());
            statement2.setString(2, professore.materia());
            statement2.executeUpdate();

            return true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Controlla se lo username esiste già
    public boolean usernameUtilizzato(String username) {
        String query = "SELECT username FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Verifica le credenziali dell'utente
    public boolean validateCredentials(String username, String password) {
        String query = "SELECT password FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return BCryptService.verifyPassword(password, resultSet.getString("password"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return false;
    }

    // Restituisce il tipo di utente (studente/professore)
    public String getTypeUser(String username) {
        String query = "SELECT tipo FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("tipo");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Restituisce nome e cognome completo
    public String getFullName(String username) {
        String query = "SELECT nome, cognome FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("nome") + " " + resultSet.getString("cognome");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Restituisce la materia di un professore
    public String getMateriaProf(String username) {
        String query = "SELECT materia FROM profMateria WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("materia");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Restituisce la classe di appartenenza di un utente
    public String getClasseUser(String username) {
        String query = "SELECT classeAppartenenza FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("classeAppartenenza");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Restituisce la data di nascita di un utente
    public String getDataNascita(String username) {
        String query = "SELECT dataNascita FROM user WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getString("dataNascita");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return "";
    }

    // Aggiorna la classe di appartenenza di un utente
    public boolean updateClasseUser(String username, String newClasse) {
        String query = "UPDATE user SET classeAppartenenza = ? WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, newClasse);
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}