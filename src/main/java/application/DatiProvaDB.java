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

    private static final String[] CLASSI = {
            "1A", "2A", "3A", "4A", "5A"
    };

    private static final String[] NOMI = {
            "Alessandro", "Sofia", "Lorenzo", "Giulia", "Francesco",
            "Aurora", "Leonardo", "Alice", "Matteo", "Ginevra",
            "Andrea", "Emma", "Gabriele", "Giorgia", "Riccardo",
            "Beatrice", "Tommaso", "Greta", "Edoardo", "Vittoria",
            "Federico", "Martina", "Michele", "Chiara", "Antonio"
    };

    private static final String[] COGNOMI = {
            "Rossi", "Russo", "Ferrari", "Esposito", "Bianchi",
            "Romano", "Colombo", "Ricci", "Marino", "Greco",
            "Bruno", "Gallo", "Conti", "De Luca", "Mancini",
            "Costa", "Giordano", "Rizzo", "Lombardi", "Moretti",
            "Barbieri", "Fontana", "Santoro", "Mariani", "Rinaldi"
    };

    public static void main(String[] args) {
        // 1. Reset del DB
        resetDatabase();

        // 2. Inizializzazione
        Database db = Database.getInstance();

        System.out.println("--- Inizio popolamento DB massivo ---");

        insertMaterie();
        insertCodiciClasse(); // Logica codici invariata
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

        // --- MATEMATICA (prof1) ---
        // Biennio
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-10-10", "Esercizi pag. 45 n. 1, 2, 3 (Equazioni)", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-10-24", "Studio del segno della parabola", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-11-12", "Esercizi sulle disequazioni fratte", "2A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-11-28", "Problemi di geometria analitica", "2A"));
        // Triennio (Nuovi inserimenti)
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-10-15", "Goniometria: Formule di addizione", "3A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-11-05", "Verifica su esponenziali e logaritmi", "4A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-12-01", "Studio di funzione: limiti e derivate", "5A"));
        db.insertCompito(new CompitoAssegnato(0, "prof1", "Matematica", "2025-12-15", "Simulazione seconda prova esame di stato", "5A"));


        // --- ITALIANO (prof2) ---
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-10-15", "Parafrasi canto I dell'Inferno", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-11-18", "Analisi del testo: 'La coscienza di Zeno'", "2A"));
        // Triennio
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-10-20", "Il Decameron: riassunto novelle scelte", "3A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-11-25", "Illuminismo e Parini: 'Il Giorno'", "4A"));
        db.insertCompito(new CompitoAssegnato(0, "prof2", "Italiano", "2025-12-10", "Leopardi: Analisi de 'L'Infinito' e pessimismo cosmico", "5A"));


        // --- INGLESE (prof3) ---
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-10-18", "Grammar: Present Perfect vs Past Simple", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-12-10", "Literature: Shakespeare's Macbeth summary", "2A"));
        // Triennio
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-11-05", "The Victorian Age: Dickens and social reforms", "4A"));
        db.insertCompito(new CompitoAssegnato(0, "prof3", "Inglese", "2025-12-02", "Modernism: James Joyce and Stream of Consciousness", "5A"));


        // --- STORIA (prof4) ---
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-10-20", "Studiare la Rivoluzione Francese (pag. 120-140)", "1A"));
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-12-12", "I moti rivoluzionari del 1848", "2A"));
        // Triennio
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-11-15", "La Prima Guerra Mondiale: cause e conseguenze", "5A"));
        db.insertCompito(new CompitoAssegnato(0, "prof4", "Storia", "2025-12-18", "Il Fascismo e le leggi razziali", "5A"));

        // --- SCIENZE (prof5) ---
        db.insertCompito(new CompitoAssegnato(0, "prof5", "Scienze", "2025-10-22", "La tettonica delle placche", "3A"));
        db.insertCompito(new CompitoAssegnato(0, "prof5", "Scienze", "2025-11-10", "Chimica Organica: Alcani e Alcheni", "5A"));
    }

    private static void insertAssenzeMassive(Database db) {
        System.out.println("--- Inserimento Assenze ---");

        // CLASSE 1A (stud1 - stud5)
        addAssenzaHelper(db, "stud1", 5, 10, "Visita dentistica");
        addAssenzaHelper(db, "stud1", 20, 10, null); // Ingiustificata
        addAssenzaHelper(db, "stud2", 12, 10, "Motivi familiari");

        // CLASSE 2A (stud6 - stud10)
        addAssenzaHelper(db, "stud6", 15, 11, "Influenza");
        addAssenzaHelper(db, "stud6", 16, 11, "Convalescenza");
        addAssenzaHelper(db, "stud7", 02, 12, null); // Ingiustificata

        // CLASSE 3A (stud11 - stud15)
        addAssenzaHelper(db, "stud11", 10, 10, "Visita sportiva agonistica");
        addAssenzaHelper(db, "stud13", 14, 11, null); // Ritardo?

        // CLASSE 4A (stud16 - stud20)
        addAssenzaHelper(db, "stud16", 20, 11, "Rinnovo patente/documenti");
        addAssenzaHelper(db, "stud18", 05, 12, "Sciopero treni");

        // CLASSE 5A (stud21 - stud25)
        addAssenzaHelper(db, "stud21", 01, 10, "Orientamento universitario");
        addAssenzaHelper(db, "stud21", 02, 10, "Orientamento universitario");
        addAssenzaHelper(db, "stud25", 22, 12, "Partenza anticipata vacanze"); // Giustificata dai genitori

        System.out.println("[OK] Assenze distribuite su tutte le classi.");
    }

    private static void addAssenzaHelper(Database db, String stud, int g, int m, String motivazione) {
        Assenza a = new Assenza(stud, g, m, 2025, null, false);
        db.addAssenza(a); // Inserisce come NON giustificata

        if (motivazione != null) {
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
                    "20-01-1980"
            );
            Professore professore = new Professore(userProf, "1A", materia);

            if (!db.usernameUtilizzato(username)) {
                db.insertProfessore(professore);
            }
            profIndex++;
        }

        // STUDENTI (25 studenti totali, 5 per ogni classe)
        // stud1-5 -> 1A, stud6-10 -> 2A, stud11-15 -> 3A, etc.
        for (int i = 0; i < 25; i++) {
            int studentNum = i + 1;
            String username = "stud" + studentNum;

            // Determina la classe in base all'indice (gruppi di 5)
            // 0-4 -> index 0 (1A), 5-9 -> index 1 (2A)...
            String classeAssegnata = CLASSI[i / 5];

            String nome = NOMI[nameIndex % NOMI.length];
            String cognome = COGNOMI[nameIndex % COGNOMI.length];
            nameIndex++;

            User userStud = new User(
                    username, nome, cognome,
                    BCryptService.hashPassword(commonPassword),
                    "2007-03-15"
            );
            Studente studente = new Studente(userStud, classeAssegnata);

            if (!db.usernameUtilizzato(username)) {
                db.insertStudente(studente);
                System.out.println("Inserito " + username + " in classe " + classeAssegnata);
            }
        }
    }

    private static void insertCodiciClasse() {
        Connection connection = DatabaseConnection.getInstance().getConnection();
        try (Statement statement = connection.createStatement()) {
            // Codici Studenti (invariati: 001 per 1A ... 005 per 5A)
            for (int i = 1; i <= 5; i++) {
                String classe = i + "A";
                String code = String.format("%03d", i);
                insertCodiceSafe(statement, classe, "studente", code);
            }

            insertCodiceSafe(statement, "1A", "professore", "006");

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