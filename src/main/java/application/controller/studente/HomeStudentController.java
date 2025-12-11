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

    // Metodo per per mostrare la pagina delle performance dello studente
    @FXML
    public void performanceClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setAndamentoPage();
    }

    // Metodo per per mostrare la pagina dei compiti assegnati allo studente
    @FXML
    public void assignmentClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setCompitiPage();
    }

    // Metodo per per mostrare la pagina delle note disciplinari dello studente
    @FXML
    public void notesButtonClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click su "Note disciplinari"
        SceneHandler.getInstance().setNotePage();
    }

    // Metodo per mostrare la pagina delle assenze dello studente
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        // Naviga alla pagina delle assenze dello studenteq
        SceneHandler.getInstance().setAssenzeStudentePage();
    }

    // Metodo per effettuare il logout dello studente
    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        // Naviga alla pagina di login
        SceneHandler.getInstance().setLoginPage();
    }

    public void initialize() {

        // Carica e imposta il logo dell'istituto
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));

        // Recupera le informazioni dello studente
        String studentInfoText = Database.getInstance().getFullName(SceneHandler.getInstance().getUsername());
        studentInfo.setText(studentInfoText.toUpperCase());

        // Recupera e imposta la classe dello studente
        String classe = Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername());
        classeStudente.setText(classe.toUpperCase());
    }
}
