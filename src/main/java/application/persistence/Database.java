package application.persistence;

import application.dao.*;
import application.model.*;
import application.observer.DatabaseObserver;
import application.observer.DatabaseSubject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

// Classe Database: Singleton che gestisce la connessione al database
// e funge da soggetto nell'Observer Pattern
public class Database implements DatabaseSubject {

    // Singleton
    private static Database instance;

    // Costruttore privato per Singleton
    private Database() {
        System.out.println("Connessione al DB in corso...");
        connect();
    }

    public static Database getInstance() {

        if (instance == null)
            instance = new Database();

        return instance;
    }


    // Lista di observer registrati che verranno notificati in caso di cambiamenti
    private final List<DatabaseObserver> observers = new ArrayList<>();

    // DAOs
    private UserDAO userDAO;
    private SchoolDAO schoolDAO;
    private VotiDAO votiDAO;
    private AssenzeDAO assenzeDAO;
    private NoteDAO noteDAO;
    private CompitiDAO compitiDAO;


    private void connect() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        Connection connection = dbConnection.getConnection();


        // Inizializza i DAO solo se la connessione è riuscita
        if (connection != null) {
            this.userDAO = new UserDAO();
            this.schoolDAO = new SchoolDAO();
            this.votiDAO = new VotiDAO();
            this.assenzeDAO = new AssenzeDAO();
            this.noteDAO = new NoteDAO();
            this.compitiDAO = new CompitiDAO();

        } else {
            System.out.println("Connessione al DB fallita");
        }
    }

    // === Metodi delegati ai DAO ===


    // Ottiene la tipologia di utente (Studente o Professore) e la classe in base al codice di iscrizione
    public TipologiaClasse getTipologiaUtente(String codiceIscrizione) {
        return schoolDAO.getTipologiaUtente(codiceIscrizione);
    }

    // Inserisce un nuovo studente nel database e inizializza i voti per tutte le materie
    public boolean insertStudente(Studente studente) {
        boolean inserted = userDAO.insertStudente(studente);
        if (inserted) {
            List<String> materie = schoolDAO.getMaterie();
            for (String materia : materie) {
                votiDAO.initializeVoto(studente.user().username(), materia);
            }
        }
        return inserted;
    }

    // Inserisce un nuovo professore nel database
    public boolean insertProfessore(Professore professore) {

        return userDAO.insertProfessore(professore);
    }

    // Controlla se uno username è già in uso
    public boolean usernameUtilizzato(String username) {

        return userDAO.usernameUtilizzato(username);
    }

    // Controlla se un codice di iscrizione è valido
    public boolean codiceIscrizioneValido(String newValue) {

        return schoolDAO.codiceIscrizioneValido(newValue);
    }

    // Verifica le credenziali dell'utente
    public boolean validateCredentials(String username, String password) {

        return userDAO.validateCredentials(username, password);
    }

    // Recupera il tipo di utente (studente o professore) in base allo username
    public String getTypeUser(String username) {

        return userDAO.getTypeUser(username);
    }

    // Recupera il nome completo dell'utente (esempio: "Mario Rossi")
    public String getFullName(String username) {

        return userDAO.getFullName(username);
    }

    // Recupera la materia insegnata dal professore
    public String getMateriaProf(String username) {

        return userDAO.getMateriaProf(username);
    }

    // Recupera la tipologia di utente in base al codice di iscrizione
    public String tipologiaUser(String codiceIscrizione) {

        return schoolDAO.tipologiaUser(codiceIscrizione);
    }

    // Recupera la classe associata a uno username
    public String getClasseUser(String username) {

        return userDAO.getClasseUser(username);
    }

    // Recupera la lista di studenti di una classe per una materia specifica
    public List<StudenteTable> getStudentiClasse(String classe, String materia) {
        return votiDAO.getStudentiClasse(classe, materia);
    }

    // Recupera tutte le materie presenti nell'istituto
    public List<String> getAllMaterieIstituto() {

        return schoolDAO.getAllMaterieIstituto();
    }

    // Inserisce una nuova nota per uno studente e notifica gli observer
    public boolean insertNota(Nota nota) {
        boolean result = noteDAO.insertNota(nota);
        if (result) {
            notifyObservers(new DatabaseEvent(DatabaseEventType.NOTA_INSERITA, nota.studente()));
        }
        return result;
    }

    // Aggiorna un voto per uno studente e notifica gli observer
    public boolean updateVoto(ValutazioneStudente valutazione) {
        boolean result = votiDAO.updateVoto(valutazione);
        if (result) {
            // Notifica specifica con tipo e dati (lo studente coinvolto)
            notifyObservers(new DatabaseEvent(DatabaseEventType.VOTO_AGGIORNATO, valutazione.studente()));
        }
        return result;
    }

    // Inserisce un nuovo compito assegnato e notifica gli observer
    public boolean insertCompito(CompitoAssegnato compito) {
        boolean result = compitiDAO.insertCompito(compito);
        if (result) {
            // Notifica per nuovo compito assegnato alla classe
            notifyObservers(new DatabaseEvent(DatabaseEventType.NUOVO_COMPITO, compito.classe()));
        }
        return result;
    }

    // Recupera i compiti assegnati a una classe specifica
    public List<CompitoAssegnato> getCompitiClasse(String classe) {

        return compitiDAO.getCompitiClasse(classe);
    }

    // Recupera i voti di uno studente
    public List<ValutazioneStudente> getVotiStudente(String studente) {

        return votiDAO.getVotiStudente(studente);
    }

    // Recupera le assenze di uno studente
    public List<Assenza> getAssenzeStudente(String studente) {

        return assenzeDAO.getAssenzeStudente(studente);
    }

    // Recupera le assenze di uno studente per un mese specifico
    public List<Assenza> getAssenzeStudente(String studente, int m) {

        return assenzeDAO.getAssenzeStudente(studente, m);
    }

    // Aggiunge una nuova assenza per uno studente e notifica gli observer
    public void addAssenza(Assenza assenza) {
        assenzeDAO.addAssenza(assenza);
        // Notifica aggiunta assenza passando lo username dello studente
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_AGGIUNTA, null));
    }

    // Giustifica un'assenza e notifica gli observer
    public void justifyAssenza(Assenza assenza, String motivazione) {
        assenzeDAO.justifyAssenza(assenza, motivazione);
        // Notifica giustificazione assenza
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_GIUSTIFICATA, null));
    }

    // Elimina un'assenza specifica e notifica gli observer
    public void deleteAssenza(String studente, int giorno, int mese, int anno) {
        assenzeDAO.deleteAssenza(studente, giorno, mese, anno);
        // Notifica eliminazione assenza
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_ELIMINATA, null));
    }

    // Recupera le note disciplinari di uno studente
    public List<Nota> getNoteStudente(String studente) {

        return noteDAO.getNoteStudente(studente);
    }

    // Recupera la data di nascita di un utente
    public String getDataNascita(String username) {

        return userDAO.getDataNascita(username);
    }

    // Inserisce un elaborato caricato per un compito specifico
    public boolean insertElaborato(ElaboratoCaricato elaborato) {

        return compitiDAO.insertElaborato(elaborato);
    }

    // Recupera gli elaborati caricati per un compito specifico
    public List<ElaboratoCaricato> getElaboratiCompito(int compitoId) {

        return compitiDAO.getElaboratiCompito(compitoId);
    }

    // Controlla se esistono elaborati per un compito specifico
    public boolean hasElaboratiForCompito(int compitoId) {

        return compitiDAO.hasElaboratiForCompito(compitoId);
    }

    // Elimina un compito specifico
    public boolean deleteCompito(int compitoId) {

        return compitiDAO.deleteCompito(compitoId);
    }

    // Elimina un elaborato specifico
    public boolean deleteElaborato(int elaboratoId) {

        return compitiDAO.deleteElaborato(elaboratoId);
    }

    // Recupera tutti i nomi delle classi per studenti
    public List<String> getAllClassiNames() {

        return schoolDAO.getAllClassiNames();
    }

    // Aggiorna la classe di uno studente
    public boolean updateClasseUser(String username, String newClasse) {
        return userDAO.updateClasseUser(username, newClasse);
    }

    // === IMPLEMENTAZIONE OBSERVER PATTERN (Soggetto Concreto) ===

    // Registra un observer alla lista
    @Override
    public void attach(DatabaseObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    // Rimuove un observer dalla lista
    @Override
    public void detach(DatabaseObserver observer) {

        observers.remove(observer);
    }

    // Notifica tutti gli observer registrati di un evento
    @Override
    public void notifyObservers(DatabaseEvent event) {
        // Firma aggiornata per accettare DatabaseEvent
        for (DatabaseObserver observer : new ArrayList<>(observers)) {
            System.out.println("Notificando observer: " + observer + " | Evento: " + event.type());
            observer.update(event); // Chiamata al metodo update dell'interfaccia
        }
    }
}
