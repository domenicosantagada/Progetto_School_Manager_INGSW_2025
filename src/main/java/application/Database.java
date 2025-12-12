package application;

import application.dao.*;
import application.model.*;
import application.observer.DataObserver;
import application.observer.ObservableSubject;

import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;

public class Database implements ObservableSubject {

    // Singleton
    private static final Database instance = new Database();
    // Lista di observer registrati che verranno notificati in caso di cambiamenti
    private final List<DataObserver> observers = new ArrayList<>();
    // DAOs
    private UserDAO userDAO;
    private SchoolDAO schoolDAO;
    private VotiDAO votiDAO;
    private AssenzeDAO assenzeDAO;
    private NoteDAO noteDAO;
    private CompitiDAO compitiDAO;

    // Costruttore privato per Singleton
    private Database() {
        System.out.println("Connessione al DB in corso...");
        connect();
    }

    public static Database getInstance() {
        return instance;
    }

    private void connect() {
        DatabaseConnection dbConnection = DatabaseConnection.getInstance();
        Connection connection = dbConnection.getConnection();

        if (connection != null) {
            this.userDAO = new UserDAO();
            this.schoolDAO = new SchoolDAO();
            this.votiDAO = new VotiDAO();
            this.assenzeDAO = new AssenzeDAO();
            this.noteDAO = new NoteDAO();
            this.compitiDAO = new CompitiDAO();

            System.out.println("Connessione al DB riuscita");
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
            notifyObservers(nota.studente());
        }
        return result;
    }

    public boolean updateVoto(ValutazioneStudente valutazione) {
        boolean result = votiDAO.updateVoto(valutazione);
        if (result) {
            notifyObservers(valutazione.studente());
        }
        return result;
    }

    public boolean insertCompito(CompitoAssegnato compito) {
        System.out.println("Compito inserito correttamente");
        return compitiDAO.insertCompito(compito);

    }

    public List<CompitoAssegnato> getCompitiClasse(String classe) {

        return compitiDAO.getCompitiClasse(classe);
    }

    public List<ValutazioneStudente> getVotiStudente(String studente) {

        return votiDAO.getVotiStudente(studente);
    }

    public void addAssenza(Assenza assenza) {
        assenzeDAO.addAssenza(assenza);
        notifyObservers("ASSENZA_AGGIUNTA");
    }

    public List<Assenza> getAssenzeStudente(String studente) {

        return assenzeDAO.getAssenzeStudente(studente);
    }

    public List<Assenza> getAssenzeStudente(String studente, int m) {

        return assenzeDAO.getAssenzeStudente(studente, m);
    }

    public void justifyAssenza(Assenza assenza, String motivazione) {

        assenzeDAO.justifyAssenza(assenza, motivazione);
        notifyObservers("ASSENZA_GIUSTIFICATA");
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

    public void deleteAssenza(String studente, int giorno, int mese, int anno) {

        assenzeDAO.deleteAssenza(studente, giorno, mese, anno);
        notifyObservers("ASSENZA_ELIMINATA");
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
