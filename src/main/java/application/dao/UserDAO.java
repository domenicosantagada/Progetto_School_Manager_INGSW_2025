package application.dao;

import application.BCryptService;
import application.DatabaseConnection;
import application.model.Professore;
import application.model.Studente;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserDAO {

    public UserDAO() {
        createTables();
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

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

    public boolean insertStudente(Studente studente) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, studente.user().username());
            statement.setString(2, studente.user().nome());
            statement.setString(3, studente.user().cognome());
            statement.setString(4, studente.user().password());
            statement.setString(5, studente.user().data());
            statement.setString(6, studente.classe());
            statement.setString(7, "studente");
            statement.executeUpdate();

            System.out.println("Studente inserito correttamente");
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public boolean insertProfessore(Professore professore) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query2 = "INSERT INTO profMateria (username, materia) VALUES (?, ?)";
        boolean result = false;
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
            System.out.println("Professore inserito correttamente");
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public boolean usernameUtilizzato(String username) {
        String query = "SELECT username FROM user WHERE username = ?";
        boolean utilizzato = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                utilizzato = true;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return utilizzato;
    }

    public boolean validateCredentials(String username, String password) {
        String query = "SELECT password FROM user WHERE username = ?";
        boolean result = false;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String passwordHash = resultSet.getString("password");
                result = BCryptService.verifyPassword(password, passwordHash);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getTypeUser(String username) {
        String query = "SELECT tipo FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("tipo");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getFullName(String username) {
        String query = "SELECT nome, cognome FROM user WHERE username = ?";
        String result = "";

        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("nome") + " " + resultSet.getString("cognome");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getMateriaProf(String username) {
        String query = "SELECT materia FROM profMateria WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("materia");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getClasseUser(String username) {
        String query = "SELECT classeAppartenenza FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("classeAppartenenza");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public String getDataNascita(String username) {
        String query = "SELECT dataNascita FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                result = resultSet.getString("dataNascita");
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    public boolean updateClasseUser(String username, String newClasse) {
        String query = "UPDATE user SET classeAppartenenza = ? WHERE username = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setString(1, newClasse);
            statement.setString(2, username);
            int rowsAffected = statement.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}
