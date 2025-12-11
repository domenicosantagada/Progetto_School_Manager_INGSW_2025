package application;

import application.model.*;
import application.observer.DataObserver;
import application.observer.ObservableSubject;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Database implements ObservableSubject {


    // Lista di observer registrati che verranno notificati in caso di cambiamenti
    private final List<DataObserver> observers = new ArrayList<>();

    private static final String DB_URL = "jdbc:sqlite:gestionale.db";

    // <<< RIGA AGGIUNTA: passphrase usata da SymmetricCryptoService >>>
    private static final String CRYPTO_PASSPHRASE = "mia-chiave-segreta-2025";

    // Singleton
    private static final Database instance = new Database();
    private Connection connection = null;

    // Costruttore privato per Singleton
    private Database() {
        System.out.println("Connessione al DB in corso...");
        connect();
    }

    public static Database getInstance() {
        return instance;
    }

    private void connect() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            if (isConnected()) {
                createTables();
                insertData();
                System.out.println("Connessione al DB riuscita");
            }
        } catch (SQLException e) {
            System.out.println("Connessione al DB fallita: " + e.getMessage());
        }
    }

    // Funzione per inserire i dati di prova
    private void insertData() {
        //insertCodiciClassi();
        //insertMaterie();
        //insertProf1A();
        //insertStudeti1A();
        //insertStudentiVoti();
    }

    private void insertStudentiVoti() {
        String query2 = "INSERT INTO studentiVoti (studente, materia, dataValutazione,voto) VALUES (?, ?, ?, ?)";
        List<String> materie = getMaterie();
        List<String> studenti = getStudenti1A();

        try (PreparedStatement statement2 = connection.prepareStatement(query2)) {
            for (String studente : studenti) {
                statement2.setString(1, studente);
                for (String materia : materie) {
                    statement2.setString(2, materia);
                    statement2.setString(3, "Nd");
                    statement2.setInt(4, 0);
                    statement2.executeUpdate();
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    private List<String> getStudenti1A() {
        String query = "SELECT username FROM user WHERE classeAppartenenza = '1A' AND tipo = 'studente'";
        List<String> studenti = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                studenti.add(resultSet.getString("username"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return studenti;
    }

    private void insertMaterie() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Matematica')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Italiano')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Inglese')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Storia')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Scienze')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Arte')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Educazione Fisica')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Religione')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Tecnologia')");
            statement.executeUpdate("INSERT INTO materie (nome) VALUES ('Musica')");
        } catch (SQLException e) {
            System.out.println("Inserimento dati fallito: " + e.getMessage());
        }
    }

    private void insertCodiciClassi() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('1A', 'studente', '001')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('2A', 'studente', '002')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('3A', 'studente', '003')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('4A', 'studente', '004')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('5A', 'studente', '005')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('1A', 'professore', '006')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('2A', 'professore', '007')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('3A', 'professore', '008')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('4A', 'professore', '009')");
            statement.executeUpdate("INSERT INTO codiciClassi (nomeClasse, tipologia, codiceAccesso) VALUES ('5A', 'professore', '010')");
        } catch (SQLException e) {
            System.out.println("Inserimento dati fallito: " + e.getMessage());
        }
    }

    private void insertProf1A() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof1', 'Mario', 'Rossi', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof1', 'Matematica')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof2', 'Luca', 'Verdi', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof2', 'Italiano')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof3', 'Paolo', 'Bianchi', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof3', 'Inglese')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof4', 'Giovanni', 'Neri', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof4', 'Stora')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof5', 'Andrea', 'Gialli', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof5', 'Scienze')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof6', 'Marco', 'Rosa', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof6', 'Arte')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof7', 'Antonio', 'Blu', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof7', 'Educazione Fisica')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof8', 'Francesco', 'Viola', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof8', 'Religione')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof9', 'Davide', 'Rosa', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof9', 'Tecnologia')");

            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('prof10', 'Roberto', 'Gialli', '0000', '01/01/1970', '1A', 'professore')");
            statement.executeUpdate("INSERT INTO profMateria (username, materia) VALUES ('prof10', 'Musica')");
        } catch (SQLException e) {
            System.out.println("Inserimento dati fallito: " + e.getMessage());
        }
    }

    private void insertStudeti1A() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud1', 'Elena', 'Ferrari', '0000', '01/01/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud2', 'Alessandro', 'Bianchi', '0000', '02/02/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud3', 'Chiara', 'Rossi', '0000', '03/03/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud4', 'Lorenzo', 'Verdi', '0000', '04/04/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud5', 'Giulia', 'Blu', '0000', '05/05/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud6', 'Mattia', 'Viola', '0000', '06/06/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud7', 'Federica', 'Neri', '0000', '07/07/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud8', 'Nicola', 'Gialli', '0000', '08/08/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud9', 'Camilla', 'Rosa', '0000', '09/09/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud10', 'Giorgio', 'De Luca', '0000', '10/10/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud11', 'Martina', 'Ferrero', '0000', '11/11/2000', '1A', 'studente')");
            statement.executeUpdate("INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES ('stud12', 'Simone', 'Pellegrini', '0000', '12/12/2000', '1A', 'studente')");
        } catch (SQLException e) {
            System.out.println("Inserimento dati fallito: " + e.getMessage());
        }
    }


    // Funzione che crea le tabelle nel database se non esistono già
    private void createTables() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate(CREATE_USER_TABLE);
            statement.executeUpdate(CREATE_CODICE_CLASSE_TABLE);
            statement.executeUpdate(CREATE_PROF_MATERIA_TABLE);
            statement.executeUpdate(CREATE_STUDENTI_VOTI_TABLE);
            statement.executeUpdate(CREATE_MATERIE_TABLE);
            statement.executeUpdate(CREATE_NOTE_TABLE);
            statement.executeUpdate(CREATE_COMPITI_TABLE);
            statement.executeUpdate(CREATE_ASSENZE_TABLE);
        } catch (SQLException e) {
            System.out.println("Creazione tabelle fallita: " + e.getMessage());
        }
    }


    // Metodo per registrare un observer
    private boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }


    // Metodo per creare la tabella user nel database
    private static final String CREATE_USER_TABLE = """ 
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

    // Metodo per creare la tabella codiciClassi nel database
    private static final String CREATE_CODICE_CLASSE_TABLE = """ 
            CREATE TABLE IF NOT EXISTS codiciClassi (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                nomeClasse TEXT NOT NULL,
                tipologia TEXT,
                codiceAccesso TEXT NOT NULL
               );
            """;

    // Metodo per creare la tabella profMateria nel database
    private static final String CREATE_PROF_MATERIA_TABLE = """ 
            CREATE TABLE IF NOT EXISTS profMateria (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                username TEXT NOT NULL,
                materia TEXT NOT NULL,
                FOREIGN KEY (username) REFERENCES user(username)
            );
            """;

    // Metodo per creare la tabella studentiVoti nel database
    private static final String CREATE_STUDENTI_VOTI_TABLE = """
            CREATE TABLE IF NOT EXISTS studentiVoti (
                studente TEXT NOT NULL,
                materia TEXT NOT NULL,
                dataValutazione TEXT NOT NULL,
                voto INTEGER NOT NULL,
                PRIMARY KEY (studente, materia),
                FOREIGN KEY (studente) REFERENCES user(username),
                FOREIGN KEY (materia) REFERENCES materie(nome)
            );
            """;

    // Metodo per creare la tabella materie nel database
    private static final String CREATE_MATERIE_TABLE = """
            CREATE TABLE IF NOT EXISTS materie (
            	id INTEGER PRIMARY KEY AUTOINCREMENT,
            	nome TEXT NOT NULL
            );
            """;

    // Metodo per creare la tabella note nel database
    private static final String CREATE_NOTE_TABLE = """
            CREATE TABLE IF NOT EXISTS note (
                id INTEGER PRIMARY KEY AUTOINCREMENT,
                studente TEXT NOT NULL,
                professore TEXT NOT NULL,
                nota TEXT NOT NULL,
                dataInserimento TEXT NOT NULL,
                FOREIGN KEY (studente) REFERENCES user(username),
                FOREIGN KEY (professore) REFERENCES user(username)
            );
            """;

    // Metodo per creare la tabella compiti nel database
    private static final String CREATE_COMPITI_TABLE = """
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

    // Metodo per creare la tabella assenze nel database
    private static final String CREATE_ASSENZE_TABLE = """
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

    // Metodo per ottenere la tipologia e la classe dell'utente in base al codice di iscrizione
    // Es. se il codice di iscrizione è 001 ritorna tipologia: studente, classe: 1A
    public TipologiaClasse getTipologiaUtente(String codiceIscrizione) {
        String query = "SELECT tipologia, nomeClasse FROM codiciClassi WHERE codiceAccesso = ?";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per inserire uno studente nel database
    public boolean insertStudente(Studente studente) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query2 = "INSERT INTO studentiVoti (studente, materia, dataValutazione,voto) VALUES (?, ?, ?, ?)";
        List<String> materie = getMaterie();
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query);
             PreparedStatement statement2 = connection.prepareStatement(query2)) {
            statement.setString(1, studente.user().username());
            statement.setString(2, studente.user().nome());
            statement.setString(3, studente.user().cognome());
            statement.setString(4, studente.user().password());
            statement.setString(5, studente.user().data());
            statement.setString(6, studente.classe());
            statement.setString(7, "studente");
            statement.executeUpdate();
            statement2.setString(1, studente.user().username());

            // Inserimento voci iniziali nella tabella studentiVoti per ogni materia
            for (String materia : materie) {
                statement2.setString(2, materia);
                statement2.setString(3, "Nd");
                statement2.setInt(4, 0);
                statement2.executeUpdate();
            }
            System.out.println("Studente inserito correttamente");
            result = true;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Metodo per ottenere la lista delle materie presenti nel database
    private List<String> getMaterie() {
        String query = "SELECT nome FROM materie";
        List<String> materie = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                materie.add(resultSet.getString("nome"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return materie;
    }

    // Metodo per inserire un professore nel database
    public boolean insertProfessore(Professore professore) {
        String query = "INSERT INTO user (username, nome, cognome, password, dataNascita, classeAppartenenza, tipo) VALUES (?, ?, ?, ?, ?, ?, ?)";
        String query2 = "INSERT INTO profMateria (username, materia) VALUES (?, ?)";
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query);
             PreparedStatement statement2 = connection.prepareStatement(query2)) {
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

    // Metodo per verificare se un username è già utilizzato
    public boolean usernameUtilizzato(String username) {
        String query = "SELECT username FROM user WHERE username = ?";
        boolean utilizzato = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per verificare se un codice di iscrizione è valido
    public boolean codiceIscrizioneValido(String newValue) {
        String query = "SELECT codiceAccesso FROM codiciClassi WHERE codiceAccesso = ?";
        boolean valido = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per validare le credenziali di accesso di un utente
    public boolean validateCredentials(String username, String password) {
        String query = "SELECT password FROM user WHERE username = ?";
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, username);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                String passwordHash = resultSet.getString("password");
                // <<< RIGA MODIFICATA: verifica con SymmetricCryptoService usando la passphrase >>>
                result = AESGCMCryptoService.verifyPassword(password, passwordHash, CRYPTO_PASSPHRASE);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Metodo per ottenere il tipo di utente (studente o professore) in base all'username
    public String getTypeUser(String username) {
        String query = "SELECT tipo FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere il nome completo di un utente in base all'username
    public String getFullName(String username) {
        String query = "SELECT nome, cognome FROM user WHERE username = ?";
        String result = "";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere la materia insegnata da un professore in base all'username
    public String getMateriaProf(String username) {
        String query = "SELECT materia FROM profMateria WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere la tipologia di utente in base al codice di iscrizione
    public String tipologiaUser(String codiceIscrizione) {
        String query = "SELECT tipologia FROM codiciClassi WHERE codiceAccesso = ?";
        String result = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere la classe di appartenenza di un utente in base all'username
    public String getClasseUser(String username) {
        String query = "SELECT classeAppartenenza FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere la lista degli studenti di una classe con le loro valutazioni in una materia specifica
    public List<StudenteTable> getStudentiClasse(String classe, String materia) {
        List<StudenteTable> studenti = new ArrayList<>();
        String query = """
                SELECT u.nome, u.cognome, sv.dataValutazione, sv.voto, u.username
                FROM user u
                JOIN studentiVoti sv ON u.username = sv.studente
                WHERE u.classeAppartenenza = ? AND sv.materia = ?
                ORDER BY u.cognome, u.nome, sv.dataValutazione
                """;

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, classe);
            statement.setString(2, materia);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String nome = resultSet.getString("nome");
                String cognome = resultSet.getString("cognome");
                String dataValutazione = resultSet.getString("dataValutazione");
                Integer voto = resultSet.getInt("voto");
                String username = resultSet.getString("username");
                studenti.add(new StudenteTable(nome, cognome, dataValutazione, voto, username));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Errore durante il recupero degli studenti: " + e.getMessage(), e);
        }
        return studenti;
    }

    // Metodo per ottenere tutte le materie presenti nell'istituto
    public List<String> getAllMaterieIstituto() {
        String query = "SELECT nome FROM materie";
        List<String> materie = new ArrayList<>();
        try (Statement statement = connection.createStatement()) {
            ResultSet resultSet = statement.executeQuery(query);
            while (resultSet.next()) {
                materie.add(resultSet.getString("nome"));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return materie;
    }

    // Metodo per inserire una nota nel database
    public boolean insertNota(Nota nota) {
        String query = "INSERT INTO note (studente, professore, nota, dataInserimento) VALUES (?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, nota.studente());
            statement.setString(2, nota.prof());
            statement.setString(3, nota.nota());
            statement.setString(4, nota.data());
            statement.executeUpdate();
            result = true;

            // NOTIFICA: L'username dello studente è l'evento
            notifyObservers(nota.studente());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Metodo per aggiornare il voto di uno studente in una materia specifica
    public boolean updateVoto(ValutazioneStudente valutazione) {
        // Query che aggiorna il voto e la data di valutazione dello studente
        String query = """
                UPDATE studentiVoti
                SET voto = ?, dataValutazione = ?
                WHERE studente = ? AND materia = ?
                """;
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setInt(1, valutazione.voto());
            statement.setString(2, valutazione.data());
            statement.setString(3, valutazione.studente());
            statement.setString(4, valutazione.materia());
            statement.executeUpdate();
            result = true;

            // NOTIFICA: L'username dello studente è l'evento
            notifyObservers(valutazione.studente());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return result;
    }

    // Metodo per inserire un compito assegnato nel database
    public boolean insertCompito(CompitoAssegnato compito) {
        String query = "INSERT INTO compiti (professore, materia, data, descrizione, classe) VALUES (?, ?, ?, ?, ?)";
        boolean result = false;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere la lista dei compiti assegnati a una classe specifica
    public List<CompitoAssegnato> getCompitiClasse(String classe) {
        List<CompitoAssegnato> compiti = new ArrayList<>();
        String query = """
                SELECT professore, materia, data, descrizione, classe
                FROM compiti
                WHERE classe = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, classe);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String professore = resultSet.getString("professore");
                String materia = resultSet.getString("materia");
                String data = resultSet.getString("data");
                String descrizione = resultSet.getString("descrizione");
                String classeCompito = resultSet.getString("classe");
                compiti.add(new CompitoAssegnato(professore, materia, data, descrizione, classeCompito));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return compiti;
    }

    // Metodo per ottenere la lista dei voti di uno studente
    public List<ValutazioneStudente> getVotiStudente(String studente) {
        List<ValutazioneStudente> voti = new ArrayList<>();
        String query = """
                SELECT sv.materia, sv.dataValutazione, sv.voto, pm.username AS professore
                FROM studentiVoti sv
                JOIN profMateria pm ON sv.materia = pm.materia
                WHERE sv.studente = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String materia = resultSet.getString("materia");
                String dataValutazione = resultSet.getString("dataValutazione");
                int voto = resultSet.getInt("voto");
                String professore = resultSet.getString("professore");
                voti.add(new ValutazioneStudente(studente, professore, materia, dataValutazione, voto));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return voti;
    }

    // Metodo per aggiungere un'assenza di uno studente
    public void addAssenza(Assenza assenza) {
        String query = "INSERT INTO assenze (studente, giorno, mese, anno, motivazione, giustificata) VALUES (?, ?, ?, ?, ?, ?)";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, assenza.username());
            statement.setInt(2, assenza.giorno());
            statement.setInt(3, assenza.mese());
            statement.setInt(4, assenza.anno());
            statement.setString(5, "Assenza non giustificata");
            statement.setBoolean(6, false);
            statement.executeUpdate();

            // NOTIFICA: Usiamo una stringa generica per le assenze (potrebbe essere migliorata)
            notifyObservers("ASSENZA_AGGIUNTA");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo per ottenere tutte le assenze di uno studente
    public List<Assenza> getAssenzeStudente(String studente) {
        List<Assenza> assenze = new ArrayList<>();
        String query = """
                SELECT giorno, mese, anno, motivazione, giustificata
                FROM assenze
                WHERE studente = ?
                ORDER BY anno DESC, mese DESC, giorno DESC
                """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per ottenere le assenze di uno studente in un mese specifico
    public List<Assenza> getAssenzeStudente(String studente, int m) {
        List<Assenza> assenze = new ArrayList<>();
        String query = """
                SELECT giorno, mese, anno, motivazione, giustificata
                FROM assenze
                WHERE studente = ? AND mese = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // Metodo per giustificare un'assenza
    public void justifyAssenza(Assenza assenza, String motivazione) {
        String query = """
                UPDATE assenze
                SET motivazione = ?, giustificata = 1
                WHERE studente = ? AND giorno = ? AND mese = ? AND anno = ?
                """;
        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, motivazione);
            statement.setString(2, assenza.username());
            statement.setInt(3, assenza.giorno());
            statement.setInt(4, assenza.mese());
            statement.setInt(5, assenza.anno());
            statement.executeUpdate();

            notifyObservers("ASSENZA_GIUSTIFICATA");
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    // Metodo per ottenere le note di uno studente
    public List<Nota> getNoteStudente(String studente) {
        List<Nota> note = new ArrayList<>();
        String query = "SELECT professore, nota, dataInserimento FROM note WHERE studente = ?";

        try (PreparedStatement statement = connection.prepareStatement(query)) {
            statement.setString(1, studente);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                String professore = resultSet.getString("professore");
                String nota = resultSet.getString("nota");
                String dataInserimento = resultSet.getString("dataInserimento");
                note.add(new Nota(studente, professore, nota, dataInserimento));
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return note;
    }

    // Metodo per ottenere la data di nascita di un utente in base all'username
    public String getDataNascita(String username) {
        String query = "SELECT dataNascita FROM user WHERE username = ?";
        String result = "";
        try (PreparedStatement statement = connection.prepareStatement(query)) {
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

    // === IMPLEMENTAZIONE OBSERVER PATTERN (Soggetto Concreto) ===

    @Override
    public void attach(DataObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void detach(DataObserver observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers(Object event) {
        // Notifica tutti gli observer registrati
        for (DataObserver observer : observers) {
            observer.update(event);
        }
    }
}