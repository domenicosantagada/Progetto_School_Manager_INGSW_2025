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

    @FXML
    private Label nominativoStudente;

    @FXML
    private VBox noteContainer;

    @FXML
    private Label classeStudente;

    private String studente;
    private String classe;
    private List<Nota> noteStudente = null;

    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        studente = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(studente);
        noteStudente = Database.getInstance().getNoteStudente(studente);

        nominativoStudente.setText(Database.getInstance().getFullName(studente).toUpperCase());
        classeStudente.setText(classe.toUpperCase());

        visualizzaNote();
    }

    private void visualizzaNote() {
        if (!noteStudente.isEmpty() && noteStudente != null) {
            noteContainer.getChildren().clear();
            for (Nota nota : noteStudente)
                generaLabel(nota);
        }
    }

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

        /*aggiungi stile al border pane*/
        newBorderPane.getStyleClass().add("compitiPane");
        /*aggiungo stile al label nel top*/
        prof.getStyleClass().add("materiaLabel");
        /*aggiungo stile al label nel center*/
        testo.getStyleClass().add("messageLabel");
        /*aggiungo stile al label nel bottom*/
        data.getStyleClass().add("dateLabel");


        noteContainer.getChildren().add(newBorderPane);
    }
}