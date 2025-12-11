package application.controller.prof;

import application.Database;
import application.SceneHandler;
import application.model.CompitoAssegnato;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class ConsegneController {


    private String prof;
    private String classe;


    @FXML
    private VBox consegneContainer;
    @FXML
    private Label classeLabel;

    // Metodo per tornare alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);

        classeLabel.setText(classe.toUpperCase());
        

        visualizzaCompiti();
    }

    // Metodo per visualizzare i compiti assegnati
    private void visualizzaCompiti() {

    }

    private void generaLabel(CompitoAssegnato comp) {

    }
}