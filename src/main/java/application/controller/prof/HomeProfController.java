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
    private Label profInfo;          // Nome completo del professore

    @FXML
    private Label materiaProf;       // Materia insegnata dal professore

    @FXML
    private ImageView logoView;      // Logo dell'applicazione da mostrare nella home

    @FXML
    public void studentsClicked(ActionEvent actionEvent) throws IOException {
        // Reindirizza alla lista degli studenti associati al professore
        SceneHandler.getInstance().setStudentsListPage();
    }

    @FXML
    public void assgimentClicked(ActionEvent actionEvent) throws IOException {
        // Apre la sezione dedicata ai compiti/assegnazioni
        SceneHandler.getInstance().setAssignmentPage();
    }

    @FXML
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        // Mostra la pagina relativa ai voti degli studenti
        SceneHandler.getInstance().setVotesPage();
    }

    @FXML
    public void consegneClicked(ActionEvent actionEvent) throws IOException {
        // Apre la sezione dedicata alle consegne degli studenti
        SceneHandler.getInstance().setConsegnePage();
    }

    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        // Effettua il logout e ritorna alla schermata di login
        SceneHandler.getInstance().setLoginPage();
    }

    public void initialize() {
        // Carica e visualizza il logo dell'applicazione
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
        logoView.setSmooth(true);

        // Imposta il nome completo del professore nella UI
        String profInfoText = Database.getInstance().getFullName(SceneHandler.getInstance().getUsername());
        profInfo.setText(profInfoText.toUpperCase());

        // Imposta la materia del professore nella UI
        String materiaProfText = Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername());
        materiaProf.setText(materiaProfText.toUpperCase());
    }


}
