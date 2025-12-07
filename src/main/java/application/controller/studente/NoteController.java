package application.controller.studente;

import application.Database;
import application.SceneHandler;
import application.model.Nota;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class NoteController {


    private String studente;
    private String classe;
    private List<Nota> noteStudente = null;

    @FXML
    private Label nominativoStudente;
    @FXML
    private VBox noteContainer;
    @FXML
    private Label classeStudente;

    // Metodo per tornare alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        // Recupera informazioni dello studente e delle note
        studente = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(studente);
        noteStudente = Database.getInstance().getNoteStudente(studente);
        nominativoStudente.setText(Database.getInstance().getFullName(studente).toUpperCase());
        classeStudente.setText(classe.toUpperCase());

        visualizzaNote();
    }

    // Metodo per visualizzare le note disciplinari
    private void visualizzaNote() {
        // Controllo se ci sono note disciplinari
        if (!noteStudente.isEmpty() && noteStudente != null) {
            // Pulisco il container delle note
            noteContainer.getChildren().clear();

            // Genero le etichette per ogni nota
            for (Nota nota : noteStudente)
                generaLabel(nota);
        }
    }

    // Metodo per generare l'etichetta di una nota disciplinare
    private void generaLabel(Nota nota) {
        BorderPane newBorderPane = new BorderPane();
        Label prof = new Label();
        prof.setText("Prof. " + Database.getInstance().getFullName(nota.prof()).toUpperCase());
        Label testo = new Label();
        testo.setText(nota.nota());
        Label data = new Label();
        data.setText(nota.data());

        newBorderPane.setTop(prof);
        newBorderPane.setCenter(testo);
        newBorderPane.setBottom(data);
        newBorderPane.setAlignment(newBorderPane.getTop(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getBottom(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getCenter(), Pos.CENTER);

        // Aggiungo stili CSS
        newBorderPane.getStyleClass().add("compitiPane");
        prof.getStyleClass().add("materiaLabel");
        testo.getStyleClass().add("messageLabel");
        data.getStyleClass().add("dateLabel");

        // Aggiungo il BorderPane al container delle note
        noteContainer.getChildren().add(newBorderPane);
    }
}