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
    private Label classeLabel;

    @FXML
    private TextArea compitiAssegnati;

    private String materia;
    private String classe;

    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void inviaCompiti(ActionEvent actionEvent) throws IOException {
        if (compitiAssegnati.getText().trim().isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            compitiAssegnati.setText("");
        } else {
            CompitoAssegnato compito = new CompitoAssegnato(
                    SceneHandler.getInstance().getUsername(),
                    materia,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(),
                    compitiAssegnati.getText().toUpperCase(),
                    classe);

            if (Database.getInstance().insertCompito(compito)) {
                SceneHandler.getInstance().showInformation(MessageDebug.COMPITO_INSERTED);
                compitiAssegnati.setText("");
                SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());

            } else {
                SceneHandler.getInstance().showWarning(MessageDebug.COMPITO_NOT_INSERTED);
            }
            System.out.println(compito);
        }
    }

    public void initialize() {
        classe = Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername());
        classeLabel.setText(classe);
        materia = Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername());
    }
}