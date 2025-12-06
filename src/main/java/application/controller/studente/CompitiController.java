package application.controller.studente;

import application.Database;
import application.SceneHandler;
import application.model.CompitoAssegnato;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;

import java.io.IOException;
import java.util.List;

public class CompitiController {


    @FXML
    private VBox votiContainer;

    @FXML
    private Label classeLabel;

    private String studente;
    private String classe;
    private List<CompitoAssegnato> compiti = null;

    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        studente = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(studente);
        compiti = Database.getInstance().getCompitiClasse(classe);
        classeLabel.setText(classe.toUpperCase());

        visualizzaCompiti();
    }

    private void visualizzaCompiti() {
        if (!compiti.isEmpty() && compiti != null) {
            votiContainer.getChildren().clear();
            for (CompitoAssegnato comp : compiti)
                generaLabel(comp);
        }
    }

    private void generaLabel(CompitoAssegnato comp) {
        BorderPane newBorderPane = new BorderPane();
        Label materia = new Label();
        materia.setText(comp.materia().toUpperCase());
        Label message = new Label();
        message.setText(comp.descrizione());
        Label date = new Label();
        date.setText(comp.data());

        newBorderPane.setTop(materia);
        newBorderPane.setCenter(message);
        newBorderPane.setBottom(date);
        newBorderPane.setAlignment(newBorderPane.getTop(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getBottom(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getCenter(), Pos.CENTER);

        /*aggiungi stile al border pane*/
        newBorderPane.getStyleClass().add("compitiPane");
        /*aggiungo stile al label nel top*/
        materia.getStyleClass().add("materiaLabel");
        /*aggiungo stile al label nel center*/
        message.getStyleClass().add("messageLabel");
        /*aggiungo stile al label nel bottom*/
        date.getStyleClass().add("dateLabel");

        votiContainer.getChildren().add(newBorderPane);
    }
}