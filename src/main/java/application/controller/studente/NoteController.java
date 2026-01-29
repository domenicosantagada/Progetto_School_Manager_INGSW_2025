package application.controller.studente;

import application.model.Nota;
import application.persistence.Database;
import application.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class NoteController {

    private String usernameStudente;              // Username dello studente loggato
    private String classe;                // Classe dello studente
    private List<Nota> noteStudente = null; // Lista delle note disciplinari dello studente

    @FXML
    private Label nominativoStudente;     // Etichetta per il nome completo dello studente
    @FXML
    private VBox noteContainer;           // Container per visualizzare le note
    @FXML
    private Label classeStudente;         // Etichetta per la classe dello studente


    private SceneHandler sh;
    private Database db;


    // Metodo per tornare alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    // Inizializza la pagina delle note disciplinari
    @FXML
    public void initialize() {

        sh = SceneHandler.getInstance();
        db = Database.getInstance();

        usernameStudente = sh.getUsername();
        classe = db.getClasseUser(usernameStudente);
        noteStudente = db.getNoteStudente(usernameStudente);

        // Imposta nome e classe nella UI
        nominativoStudente.setText(db.getFullName(usernameStudente).toUpperCase());
        classeStudente.setText(classe.toUpperCase());

        // Visualizza le note disciplinari nella UI
        visualizzaNote();
    }

    // Metodo per visualizzare le note disciplinari
    private void visualizzaNote() {
        if (noteStudente != null && !noteStudente.isEmpty()) {
            // Pulisco il container delle note
            noteContainer.getChildren().clear();

            // Genero una card per ogni nota
            for (Nota nota : noteStudente) {
                generaLabel(nota);
            }
        }
    }

    // Metodo per generare l'etichetta di una singola nota disciplinare
    private void generaLabel(Nota nota) {
        BorderPane newBorderPane = new BorderPane();

        // Label per il professore che ha inserito la nota
        Label prof = new Label("Prof. " + Database.getInstance().getFullName(nota.prof()).toUpperCase());
        // Label per il testo della nota
        Label testo = new Label(nota.nota());
        // Label per la data della nota
        Label data = new Label(nota.data());

        // Posizionamento delle label nel BorderPane
        newBorderPane.setTop(prof);
        newBorderPane.setCenter(testo);
        newBorderPane.setBottom(data);
        newBorderPane.setAlignment(prof, Pos.CENTER);
        newBorderPane.setAlignment(testo, Pos.CENTER);
        newBorderPane.setAlignment(data, Pos.CENTER);

        // Applicazione stili CSS
        newBorderPane.getStyleClass().add("compitiPane");
        prof.getStyleClass().add("materiaLabel");
        testo.getStyleClass().add("messageLabel");
        data.getStyleClass().add("dateLabel");

        // Aggiunge la nota al container principale
        noteContainer.getChildren().add(newBorderPane);
    }
}