package application.controller.prof;

import application.persistence.Database;
import application.view.SceneHandler;
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

    // Mostra la lista degli studenti associati al professore
    @FXML
    public void studentsClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setStudentsListPage();
    }

    // Apre la sezione dei compiti/assegnazioni
    @FXML
    public void assgimentClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setAssignmentPage();
    }

    // Mostra la pagina delle assenze
    @FXML
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setAssenzePage();
    }

    // Apre la sezione delle consegne degli studenti
    @FXML
    public void consegneClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setConsegnePage();
    }

    // Effettua il logout e torna alla pagina di login
    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        SceneHandler.getInstance().setLoginPage();
    }

    // Inizializza la home del professore con logo, info e scelta della classe
    @FXML
    public void initialize() {
        // Carica il logo
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
        logoView.setSmooth(true);

        String username = SceneHandler.getInstance().getUsername();

        // Imposta nome completo e materia
        profInfo.setText(Database.getInstance().getFullName(username).toUpperCase());
        materiaProf.setText(Database.getInstance().getMateriaProf(username).toUpperCase());

        // Popola la ChoiceBox con le classi disponibili
        List<String> classi = Database.getInstance().getAllClassiNames();
        classeChoiceBox.getItems().addAll(classi);

        // Imposta la classe corrente
        String currentClass = Database.getInstance().getClasseUser(username);
        if (currentClass != null && classi.contains(currentClass)) {
            classeChoiceBox.setValue(currentClass);
        } else if (!classi.isEmpty()) {
            classeChoiceBox.setValue(classi.get(0));
            Database.getInstance().updateClasseUser(username, classi.get(0));
        }

        // Listener per aggiornare la classe nel database al cambio selezione
        classeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    Database.getInstance().updateClasseUser(username, newValue);
                    System.out.println("Classe cambiata a: " + newValue);
                }
            }
        });
    }
}
