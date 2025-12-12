package application.controller.prof;

import application.Database;
import application.SceneHandler;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;
import java.util.List;

public class HomeProfController {

    @FXML
    private Label profInfo;          // Nome completo del professore

    @FXML
    private Label materiaProf;       // Materia insegnata dal professore

    @FXML
    private ChoiceBox<String> classeChoiceBox; // Menu a tendina per la selezione della classe

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

        String username = SceneHandler.getInstance().getUsername();

        // Imposta il nome completo del professore nella UI
        String profInfoText = Database.getInstance().getFullName(username);
        profInfo.setText(profInfoText.toUpperCase());

        // Imposta la materia del professore nella UI
        String materiaProfText = Database.getInstance().getMateriaProf(username);
        materiaProf.setText(materiaProfText.toUpperCase());

        // Popola la ChoiceBox con le classi disponibili
        List<String> classi = Database.getInstance().getAllClassiNames();
        classeChoiceBox.getItems().addAll(classi);

        // Imposta la classe corrente
        String currentClass = Database.getInstance().getClasseUser(username);
        if (currentClass != null && classi.contains(currentClass)) {
            classeChoiceBox.setValue(currentClass);
        } else if (!classi.isEmpty()) {
            classeChoiceBox.setValue(classi.get(0));
            // Se la classe corrente non è valida, aggiorniamo subito con la prima disponibile
            Database.getInstance().updateClasseUser(username, classi.get(0));
        }

        // Aggiunge un listener per gestire il cambio di classe
        classeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    // Aggiorna la classe nel database per l'utente corrente
                    Database.getInstance().updateClasseUser(username, newValue);
                    System.out.println("Classe cambiata a: " + newValue);
                }
            }
        });
    }
}
