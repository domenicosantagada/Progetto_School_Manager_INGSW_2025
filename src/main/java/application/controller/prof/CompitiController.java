package application.controller.prof;

import application.Database;
import application.MessageDebug;
import application.SceneHandler;
import application.model.CompitoAssegnato;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CompitiController {

    @FXML
    private Label classLabel;

    @FXML
    private TextArea compitiAssegnati;

    // Variabili per memorizzare la materia insegnata dal docente e la classe assegnata
    private String materia;
    private String classe;

    // Torno alla home page del professore
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    // Gestisce l'evento di invio dei compiti quando viene premuto il pulsante dedicato
    @FXML
    public void inviaCompiti(ActionEvent actionEvent) throws IOException {
        // Controllo se il campo di testo dei compiti è vuoto o contiene solo spazi
        if (compitiAssegnati.getText().trim().isEmpty()) {
            // Se vuoto, mostro un avviso all'utente e pulisco il campo
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            compitiAssegnati.setText("");
        } else {
            // Creo un nuovo oggetto CompitoAssegnato con i dati correnti:
            // username del docente, materia, data odierna formattata, descrizione e classe
            CompitoAssegnato compito = new CompitoAssegnato(
                    -1, // ID non ancora assegnato
                    SceneHandler.getInstance().getUsername(),
                    materia,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(),
                    compitiAssegnati.getText().toUpperCase(), // Salvo il testo in maiuscolo per uniformità
                    classe);

            // Tento di inserire il compito nel database
            if (Database.getInstance().insertCompito(compito)) {
                // Se l'inserimento ha successo, mostro una conferma, pulisco il campo e torno alla home page
                SceneHandler.getInstance().showInformation(MessageDebug.COMPITO_INSERTED);
                compitiAssegnati.setText("");

                // Torno alla home page del professore prima -> SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
                // posso richiamare, invece, la funzione backButtonClicked per tornare alla home page
                backButtonClicked();

            } else {
                // Se l'inserimento fallisce, mostro un messaggio di errore
                SceneHandler.getInstance().showWarning(MessageDebug.COMPITO_NOT_INSERTED);
            }
        }
    }

    @FXML
    public void initialize() {
        // Recupero la classe assegnata al docente attualmente loggato tramite il Database
        classe = Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername());

        // Imposto il testo della label con il nome della classe recuperata
        classLabel.setText(classe);

        // Recupero la materia insegnata dal docente per utilizzarla nella creazione dei compiti
        materia = Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername());
    }
}