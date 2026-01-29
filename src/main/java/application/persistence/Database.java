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

    public TipologiaClasse getTipologiaUtente(String codiceIscrizione) {
        return schoolDAO.getTipologiaUtente(codiceIscrizione);
    }

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

    public boolean insertProfessore(Professore professore) {
        return userDAO.insertProfessore(professore);
    }

    public boolean usernameUtilizzato(String username) {
        return userDAO.usernameUtilizzato(username);
    }

    public boolean codiceIscrizioneValido(String newValue) {
        return schoolDAO.codiceIscrizioneValido(newValue);
    }

    public boolean validateCredentials(String username, String password) {
        return userDAO.validateCredentials(username, password);
    }

    public String getTypeUser(String username) {
        return userDAO.getTypeUser(username);
    }

    public String getFullName(String username) {
        return userDAO.getFullName(username);
    }

    public String getMateriaProf(String username) {
        return userDAO.getMateriaProf(username);
    }

    public String tipologiaUser(String codiceIscrizione) {
        return schoolDAO.tipologiaUser(codiceIscrizione);
    }

    public String getClasseUser(String username) {
        return userDAO.getClasseUser(username);
    }

    public List<StudenteTable> getStudentiClasse(String classe, String materia) {
        return votiDAO.getStudentiClasse(classe, materia);
    }

    public List<String> getAllMaterieIstituto() {
        return schoolDAO.getAllMaterieIstituto();
    }

    public boolean insertNota(Nota nota) {
        boolean result = noteDAO.insertNota(nota);
        if (result) {
            notifyObservers(new DatabaseEvent(DatabaseEventType.NOTA_INSERITA, nota.studente()));
        }
        return result;
    }

    public boolean updateVoto(ValutazioneStudente valutazione) {
        boolean result = votiDAO.updateVoto(valutazione);
        if (result) {
            // Notifica specifica con tipo e dati (lo studente coinvolto)
            notifyObservers(new DatabaseEvent(DatabaseEventType.VOTO_AGGIORNATO, valutazione.studente()));
        }
        return result;
    }

    public boolean insertCompito(CompitoAssegnato compito) {
        boolean result = compitiDAO.insertCompito(compito);
        if (result) {
            // Notifica per nuovo compito assegnato alla classe
            notifyObservers(new DatabaseEvent(DatabaseEventType.NUOVO_COMPITO, compito.classe()));
        }
        return result;
    }

    public List<CompitoAssegnato> getCompitiClasse(String classe) {
        return compitiDAO.getCompitiClasse(classe);
    }

    public List<ValutazioneStudente> getVotiStudente(String studente) {
        return votiDAO.getVotiStudente(studente);
    }

    public List<Assenza> getAssenzeStudente(String studente) {

        return assenzeDAO.getAssenzeStudente(studente);
    }

    public List<Assenza> getAssenzeStudente(String studente, int m) {

        return assenzeDAO.getAssenzeStudente(studente, m);
    }

    public void addAssenza(Assenza assenza) {
        assenzeDAO.addAssenza(assenza);
        // Notifica aggiunta assenza passando lo username dello studente
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_AGGIUNTA, null));
    }

    public void justifyAssenza(Assenza assenza, String motivazione) {
        assenzeDAO.justifyAssenza(assenza, motivazione);
        // Notifica giustificazione assenza
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_GIUSTIFICATA, null));
    }

    public void deleteAssenza(String studente, int giorno, int mese, int anno) {
        assenzeDAO.deleteAssenza(studente, giorno, mese, anno);
        // Notifica eliminazione assenza
        notifyObservers(new DatabaseEvent(DatabaseEventType.ASSENZA_ELIMINATA, null));
    }

    public List<Nota> getNoteStudente(String studente) {

        return noteDAO.getNoteStudente(studente);
    }

    public String getDataNascita(String username) {

        return userDAO.getDataNascita(username);
    }

    public boolean insertElaborato(ElaboratoCaricato elaborato) {

        return compitiDAO.insertElaborato(elaborato);
    }

    public List<ElaboratoCaricato> getElaboratiCompito(int compitoId) {

        return compitiDAO.getElaboratiCompito(compitoId);
    }


    public boolean hasElaboratiForCompito(int compitoId) {

        return compitiDAO.hasElaboratiForCompito(compitoId);
    }

    public boolean deleteCompito(int compitoId) {

        return compitiDAO.deleteCompito(compitoId);
    }

    public boolean deleteElaborato(int elaboratoId) {

        return compitiDAO.deleteElaborato(elaboratoId);
    }

    public List<String> getAllClassiNames() {
        return schoolDAO.getAllClassiNames();
    }

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
