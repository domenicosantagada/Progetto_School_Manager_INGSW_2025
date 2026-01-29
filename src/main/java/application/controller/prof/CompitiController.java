package application.controller.prof;

import application.model.CompitoAssegnato;
import application.observer.DatabaseObserver;
import application.persistence.Database;
import application.persistence.DatabaseEvent;
import application.persistence.DatabaseEventType;
import application.utility.MessageDebug;
import application.view.SceneHandler;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class CompitiController implements DatabaseObserver {

    @FXML
    private Label classLabel;

    @FXML
    private TextArea compitiAssegnati;

    private String materia, classe, profUsername;

    private Database db;
    private SceneHandler sh;


    // Inizializza il controller
    @FXML
    public void initialize() {

        sh = SceneHandler.getInstance();
        db = Database.getInstance();

        db.attach(this); // Si registra come observer del database

        profUsername = sh.getUsername();

        classe = db.getClasseUser(profUsername);
        materia = db.getMateriaProf(profUsername);

        // Setup interfaccia
        classLabel.setText(classe);
    }


    // Torna alla homepage del professore
    @FXML
    public void backButtonClicked() throws IOException {
        db.detach(this); // Rimuove l'observer dal database cosi non riceve piu' eventi''
        sh.setProfessorHomePage(profUsername);
    }

    // Invia un nuovo compito assegnato
    @FXML
    public void inviaCompiti(ActionEvent actionEvent) throws IOException {
        if (compitiAssegnati.getText().trim().isEmpty()) {
            sh.showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            compitiAssegnati.setText("");
            return;
        }

        CompitoAssegnato compito = new CompitoAssegnato(
                -1,
                profUsername,
                materia,
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                compitiAssegnati.getText().toUpperCase().trim(),
                classe);

        if (db.insertCompito(compito)) {
            sh.showInformation(MessageDebug.COMPITO_INSERTED);
            compitiAssegnati.setText("");
            backButtonClicked();
        } else {
            sh.showWarning(MessageDebug.COMPITO_NOT_INSERTED);
        }
    }


    // Gestisce notifiche del database
    @Override
    public void update(DatabaseEvent event) {
        if (event.type() == DatabaseEventType.NUOVO_COMPITO) {
            Platform.runLater(() -> {
                System.out.println("Notifica: Un nuovo compito è stato inserito per la classe " + event.data());
                // Possibile aggiornamento di una lista di compiti se presente
            });
        }
    }
}
