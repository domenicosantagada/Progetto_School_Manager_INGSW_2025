package application;

import application.model.*;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Classe utility per popolare il DB con molti dati di prova realistici.
 */
public class DatiProvaDB {

    private static final String DB_FILE_NAME = "gestionale.db";

    // Materie corrispondenti ai prof1...prof10
    private static final String[] MATERIE = {
            "Matematica", "Italiano", "Inglese", "Storia", "Scienze",
            "Arte", "Educazione Fisica", "Religione", "Tecnologia", "Musica"
    };

    private static final String[] NOMI = {
            "Alessandro", "Sofia", "Lorenzo", "Giulia", "Francesco",
            "Aurora", "Leonardo", "Alice", "Matteo", "Ginevra",
            "Andrea", "Emma", "Gabriele", "Giorgia", "Riccardo",
            "Beatrice", "Tommaso", "Greta", "Edoardo", "Vittoria"
    };

    private static final String[] COGNOMI = {
            "Rossi", "Russo", "Ferrari", "Esposito", "Bianchi",
            "Romano", "Colombo", "Ricci", "Marino", "Greco",
            "Bruno", "Gallo", "Conti", "De Luca", "Mancini",
            "Costa", "Giordano", "Rizzo", "Lombardi", "Moretti"
    };

    public static void main(String[] args) {
        // 1. Reset del DB
        resetDatabase();

        // 2. Inizializzazione
        Database db = Database.getInstance();

        System.out.println("--- Inizio popolamento DB massivo ---");

        insertMaterie();
        insertCodiciClasse();
        insertUtentiProva(db);
        insertCompitiMassivi(db);
        insertAssenzeMassive(db);

        System.out.println("--- DB popolato con successo! ---");
    }

    private static void resetDatabase() {
        File dbFile = new File(DB_FILE_NAME);
        if (dbFile.exists()) {
            dbFile.delete();
            System.out.println("[RESET] Database eliminato e ricreato.");
        }
    }

    private static void insertCompitiMassivi(Database db) {
        System.out.println("--- Inserimento Compiti ---");

        // MATEMATICA (prof1)
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-10-10", "Esercizi pag. 45 n. 1, 2, 3 (Equazioni)", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-10-24", "Studio del segno della parabola", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-11-12", "Esercizi sulle disequazioni fratte", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-11-28", "Problemi di geometria analitica", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-12-05", "Verifica scritta sul trimestre", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-12-19", "Esercizi di ripasso vacanze natalizie", "1A"));

        // ITALIANO (prof2)
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-10-15", "Parafrasi canto I dell'Inferno", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-10-29", "Saggio breve: 'I social network e i giovani'", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-11-10", "Leggere capitoli 1-8 dei Promessi Sposi", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-11-25", "Analisi del testo poetico: Leopardi", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-12-15", "Tema in classe: attualità", "1A"));

        // INGLESE (prof3)
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-10-18", "Grammar: Present Perfect vs Past Simple", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-11-15", "Write an essay about Global Warming (200 words)", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-12-10", "Literature: Shakespeare's Macbeth summary", "1A"));

        // STORIA (prof4)
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-10-20", "Studiare la Rivoluzione Francese (pag. 120-140)", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-11-22", "Approfondimento su Napoleone Bonaparte", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-12-12", "I moti rivoluzionari del 1848", "1A"));

        // SCIENZE (prof5)
        db.insertCompito(new CompitoAssegnato(0, "prof5", "Scienze", "2025-10-25", "Relazione sull'esperimento di chimica in laboratorio", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof5", "Scienze", "2025-11-30", "Ricerca sulle energie rinnovabili", "1A"));

        // ARTE (prof6)
        db.insertCompito(new CompitoAssegnato(0, "prof6", "Arte", "2025-11-05", "Tavola 4: Prospettiva centrale di una stanza", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof6", "Arte", "2025-12-02", "Analisi dell'opera 'La Gioconda'", "1A"));

        System.out.println("[OK] Oltre 20 compiti inseriti per diverse materie.");
    }

    private static void insertAssenzeMassive(Database db) {
        System.out.println("--- Inserimento Assenze ---");

        // Helper per aggiungere assenze rapidamente
        // Parametri: studente, giorno, mese, motivazione (se null = non giustificata)

        // STUDENTE 1 (Molte assenze, alcune giustificate)
        addAssenzaHelper(db, "stud1", 5, 10, "Visita dentistica"); // Giustificata
        addAssenzaHelper(db, "stud1", 6, 10, "Postumi intervento dentistico"); // Giustificata
        addAssenzaHelper(db, "stud1", 20, 10, null); // NON Giustificata
        addAssenzaHelper(db, "stud1", 15, 11, "Febbre alta"); // Giustificata
        addAssenzaHelper(db, "stud1", 16, 11, "Convalescenza"); // Giustificata
        addAssenzaHelper(db, "stud1", 17, 11, "Convalescenza"); // Giustificata
        addAssenzaHelper(db, "stud1", 5, 12, null); // NON Giustificata

        // STUDENTE 2 (Qualche assenza strategica)
        addAssenzaHelper(db, "stud2", 12, 10, "Motivi familiari");
        addAssenzaHelper(db, "stud2", 31, 10, null); // Ponte non ufficiale?
        addAssenzaHelper(db, "stud2", 28, 11, "Gara sportiva regionale");

        // STUDENTE 3 (Poche assenze)
        addAssenzaHelper(db, "stud3", 10, 12, "Visita specialistica");

        // STUDENTE 4 (Assenze sparse)
        addAssenzaHelper(db, "stud4", 15, 10, null);
        addAssenzaHelper(db, "stud4", 18, 11, "Sciopero dei mezzi pubblici"); // Giustificata
        addAssenzaHelper(db, "stud4", 19, 11, null);

        // STUDENTE 5
        addAssenzaHelper(db, "stud5", 22, 10, "Indisposizione");
        addAssenzaHelper(db, "stud5", 23, 10, "Indisposizione");

        System.out.println("[OK] Assenze multiple inserite per 5 studenti.");
    }

    private static void addAssenzaHelper(Database db, String stud, int g, int m, String motivazione) {
        Assenza a = new Assenza(stud, g, m, 2025, null, false);
        db.addAssenza(a); // Inserisce come NON giustificata

        if (motivazione != null) {
            // Se c'è una motivazione, la giustifichiamo subito
            db.justifyAssenza(a, motivazione);
        }
    }

    private static void insertUtentiProva(Database db) {
        String commonPassword = "0000";
        int nameIndex = 0;

        // PROFESSORI
        int profIndex = 1;
        for (String materia : MATERIE) {
            String username = "prof" + profIndex;
            String nome = NOMI[nameIndex % NOMI.length];
            String cognome = COGNOMI[nameIndex % COGNOMI.length];
            nameIndex++;

            User userProf = new User(
                    username, nome, cognome,
                    BCryptService.hashPassword(commonPassword),
                    "1978-05-20"
            );
            Professore professore = new Professore(userProf, "1A", materia);

            if (!db.usernameUtilizzato(username)) {
                db.insertProfessore(professore);
            }
            profIndex++;
        }

        // STUDENTI (10 studenti)
        for (int i = 1; i <= 10; i++) {
            String username = "stud" + i;
            String nome = NOMI[nameIndex % NOMI.length];
            String cognome = COGNOMI[nameIndex % COGNOMI.length];
            nameIndex++;

            User userStud = new User(
                    username, nome, cognome,
                    BCryptService.hashPassword(commonPassword),
                    "2007-03-15"
            );
            Studente studente = new Studente(userStud, "1A");

            if (!db.usernameUtilizzato(username)) {
                db.insertStudente(studente);
            }
        }
    }

    private static void insertCodiciClasse() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {
            // Codici Studenti
            for (int i = 1; i <= 5; i++) {
                String classe = i + "A";
                String code = String.format("%03d", i); // 001, 002...
                insertCodiceSafe(statement, classe, "studente", code);
            }
            // Codici Professori
            for (int i = 1; i <= 5; i++) {
                String classe = i + "A";
                String code = String.format("%03d", i + 5); // 006, 007...
                insertCodiceSafe(statement, classe, "professore", code);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void insertCodiceSafe(Statement stmt, String classe, String tipo, String code) {
        try {
            stmt.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('" + classe + "', '" + tipo + "', '" + code + "')");
        } catch (SQLException e) {
        } // Ignora duplicati
    }

    private static void insertMaterie() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {
            for (String materia : MATERIE) {
                try {
                    statement.executeUpdate("INSERT INTO materie (nome) VALUES ('" + materia + "')");
                } catch (SQLException ex) {
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}