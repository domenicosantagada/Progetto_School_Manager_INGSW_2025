package application.controller.prof;

import application.Database;
import application.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class HomeProfController {

    @FXML
    private Label profInfo; // Informazioni sul professore

    @FXML
    private Label materiaProf; // Materia associata al professore

    @FXML
    private ImageView logoView; // Logo visualizzato nella pagina

    @FXML
    public void studentsClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click sul pulsante "Studenti"
        SceneHandler.getInstance().setStudentsListPage();
    }

    @FXML
    public void assgimentClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click sul pulsante "Compiti"
        SceneHandler.getInstance().setAssignmentPage();
    }

    @FXML
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        // Logica per gestire il click sul pulsante "Voti"
        SceneHandler.getInstance().setVotesPage();
    }

    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        // Naviga alla pagina di login
        SceneHandler.getInstance().setLoginPage();
    }

    public void initialize() {
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
        logoView.setSmooth(true);

        // Recupera le informazioni dello studente
        String profInfoText = Database.getInstance().getFullName(SceneHandler.getInstance().getUsername());
        profInfo.setText(profInfoText.toUpperCase());

        // Recupera la materia associata al professore
        String materiaProfText = Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername());
        materiaProf.setText(materiaProfText.toUpperCase());
    }
}
