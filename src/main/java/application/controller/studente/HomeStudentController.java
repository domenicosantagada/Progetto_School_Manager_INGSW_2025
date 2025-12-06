package application.controller.studente;

import application.Database;
import application.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class HomeStudentController {

    @FXML
    private ImageView logoView;

    @FXML
    private Label studentInfo;

    @FXML
    private Label classeStudente;

    @FXML
    public void performanceClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click su "Andamento"
        SceneHandler.getInstance().setAndamentoPage();
    }

    @FXML
    public void assignmentClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click su "Compiti"
        SceneHandler.getInstance().setCompitiPage();
    }

    @FXML
    public void notesButtonClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click su "Note disciplinari"
        SceneHandler.getInstance().setNotePage();
    }

    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        // Naviga alla pagina di login
        SceneHandler.getInstance().setLoginPage();
    }

    public void initialize() {
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));

        // Recupera le informazioni dello studente
        String studentInfoText = Database.getInstance().getFullName(SceneHandler.getInstance().getUsername());
        studentInfo.setText(studentInfoText.toUpperCase());

        String classe = Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername());
        classeStudente.setText(classe.toUpperCase());
    }
}
