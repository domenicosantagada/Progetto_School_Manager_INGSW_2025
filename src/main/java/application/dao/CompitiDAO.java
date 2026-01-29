package application.dao;

import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import application.persistence.DatabaseConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CompitiDAO {

    public CompitiDAO() {
        createTables(); // Crea le tabelle compiti ed elaborati se non esistono
    }

    private Connection getConnection() {
        return DatabaseConnection.getInstance().getConnection();
    }

    // Crea le tabelle per compiti e elaborati
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

        /*String CREATE_NEW_ELABORATI_TABLE = """
                CREATE TABLE IF NOT EXISTS newElaboratiCaricati (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    compitoId INTEGER NOT NULL,
                    studente TEXT NOT NULL,
                    data TEXT NOT NULL,
                    commento TEXT,
                    file BLOB,
                    approvato BOOLEAN DEFAULT 0,
                    FOREIGN KEY (compitoId) REFERENCES compiti(id),
                    FOREIGN KEY (studente) REFERENCES user(username)
                );
                """;
         */
        try (Statement statement = getConnection().createStatement()) {
            statement.executeUpdate(CREATE_COMPITI_TABLE);
            statement.executeUpdate(CREATE_ELABORATI_TABLE);
            //statement.executeUpdate(CREATE_NEW_ELABORATI_TABLE);
        } catch (SQLException e) {
            throw new RuntimeException("Creazione tabelle Compiti fallita: " + e.getMessage(), e);
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
            return statement.executeUpdate() > 0;
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
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                compiti.add(new CompitoAssegnato(
                        rs.getInt("id"),
                        rs.getString("professore"),
                        rs.getString("materia"),
                        rs.getString("data"),
                        rs.getString("descrizione"),
                        rs.getString("classe")
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
            return statement.executeUpdate() > 0;
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
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                CompitoAssegnato compito = new CompitoAssegnato(
                        rs.getInt("compitoId"),
                        rs.getString("professore"),
                        rs.getString("materia"),
                        rs.getString("dataCompito"),
                        rs.getString("descrizione"),
                        rs.getString("classe")
                );
                elaborati.add(new ElaboratoCaricato(
                        compito,
                        rs.getString("studente"),
                        rs.getString("data"),
                        rs.getString("commento"),
                        rs.getBytes("file"),
                        rs.getInt("id") //,
                        //false
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return elaborati;
    }

    /*
    public boolean insertNewElaborato(ElaboratoCaricato elaborato) {
        String query = "INSERT INTO newElaboratiCaricati (compitoId, studente, data, commento, file, approvato) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaborato.compito().id());
            statement.setString(2, elaborato.studente());
            statement.setString(3, elaborato.data());
            statement.setString(4, elaborato.commento());
            statement.setBytes(5, elaborato.file());
            statement.setBoolean(6, false);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public List<ElaboratoCaricato> getNewElaboratiCompito(int compitoId) {
        List<ElaboratoCaricato> elaborati = new ArrayList<>();
        String query = """
                SELECT ec.id, ec.studente, ec.data, ec.commento, ec.file, ec.approvato,
                       c.id as compitoId, c.professore, c.materia, c.data as dataCompito, c.descrizione, c.classe
                FROM newElaboratiCaricati ec
                JOIN compiti c ON ec.compitoId = c.id
                WHERE ec.compitoId = ?
                """;
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            ResultSet rs = statement.executeQuery();
            while (rs.next()) {
                CompitoAssegnato compito = new CompitoAssegnato(
                        rs.getInt("compitoId"),
                        rs.getString("professore"),
                        rs.getString("materia"),
                        rs.getString("dataCompito"),
                        rs.getString("descrizione"),
                        rs.getString("classe")
                );
                elaborati.add(new ElaboratoCaricato(
                        compito,
                        rs.getString("studente"),
                        rs.getString("data"),
                        rs.getString("commento"),
                        rs.getBytes("file"),
                        rs.getInt("id"),
                        rs.getBoolean("approvato")
                ));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return elaborati;
    }
    public boolean hasnewElaboratiForCompito(int compitoId) {
        String query = "SELECT COUNT(*) FROM newElaboratiCaricati WHERE compitoId = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo degli elaborati: " + e.getMessage(), e);
        }
    }

    public boolean deletenewElaborato(int elaboratoId) {
        String query = "DELETE FROM newElaboratiCaricati WHERE id = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, elaboratoId);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante l'eliminazione dell'elaborato: " + e.getMessage(), e);
        }
    }


    */
    // Controlla se un compito ha almeno un elaborato
    public boolean hasElaboratiForCompito(int compitoId) {
        String query = "SELECT COUNT(*) FROM elaboratiCaricati WHERE compitoId = ?";
        try (PreparedStatement statement = getConnection().prepareStatement(query)) {
            statement.setInt(1, compitoId);
            ResultSet rs = statement.executeQuery();
            return rs.next() && rs.getInt(1) > 0;
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il controllo degli elaborati: " + e.getMessage(), e);
        }
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


    // Aggiunge colonna se non esiste
    public void aggiungiColonnaSeNonEsiste(String nomeTabella, String nomeColonna, String tipoColonna, boolean notNull, String defaultValue) {
        try (PreparedStatement stmt = getConnection().prepareStatement("PRAGMA table_info(" + nomeTabella + ")")) {
            ResultSet rs = stmt.executeQuery();
            boolean exists = false;
            while (rs.next()) {
                if (rs.getString("name").equalsIgnoreCase(nomeColonna)) {
                    exists = true;
                    break;
                }
            }

            if (!exists) {
                StringBuilder query = new StringBuilder();
                query.append("ALTER TABLE ").append(nomeTabella)
                        .append(" ADD COLUMN ").append(nomeColonna)
                        .append(" ").append(tipoColonna);

                if (notNull) query.append(" NOT NULL");
                if (defaultValue != null) query.append(" DEFAULT ").append(defaultValue);

                try (PreparedStatement alter = getConnection().prepareStatement(query.toString())) {
                    alter.executeUpdate();
                    System.out.println("Colonna " + nomeColonna + " aggiunta a " + nomeTabella);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore aggiungendo colonna: " + e.getMessage(), e);
        }
    }
}