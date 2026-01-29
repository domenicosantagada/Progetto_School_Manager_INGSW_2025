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


    private SceneHandler sh;
    private Database db;
    private String usernameProf;


    // Inizializza la home del professore con logo, info e scelta della classe
    @FXML
    public void initialize() {

        sh = SceneHandler.getInstance();
        db = Database.getInstance();

        usernameProf = sh.getUsername();

        caricaLogo();
        caricaInfoProfessore();
        setupClasseChoiceBox();
    }

    private void caricaLogo() {
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
        logoView.setSmooth(true);
    }

    private void caricaInfoProfessore() {
        profInfo.setText(db.getFullName(usernameProf).toUpperCase());
        materiaProf.setText(db.getMateriaProf(usernameProf).toUpperCase());
    }

    private void setupClasseChoiceBox() {
        // Popola la ChoiceBox con le classi disponibili
        List<String> classi = db.getAllClassiNames();
        classeChoiceBox.getItems().addAll(classi);

        // Imposta la classe corrente
        String currentClass = db.getClasseUser(usernameProf);
        if (currentClass != null && classi.contains(currentClass)) {
            classeChoiceBox.setValue(currentClass);
        } else if (!classi.isEmpty()) {
            classeChoiceBox.setValue(classi.get(0));
            db.updateClasseUser(usernameProf, classi.get(0));
        }

        // Listener per aggiornare la classe nel database al cambio selezione
        classeChoiceBox.getSelectionModel().selectedItemProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue != null) {
                    db.updateClasseUser(usernameProf, newValue);
                    System.out.println("Classe cambiata a: " + newValue);
                }
            }
        });
    }




    /*
    Gestione dei click sui bottoni della home del professore
     */

    // Mostra la lista degli studenti associati al professore
    @FXML
    public void studentsClicked(ActionEvent actionEvent) throws IOException {
        sh.setStudentsListPage();
    }

    // Apre la sezione dei compiti/assegnazioni
    @FXML
    public void assgimentClicked(ActionEvent actionEvent) throws IOException {
        sh.setAssignmentPage();
    }

    // Mostra la pagina delle assenze
    @FXML
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        sh.setVotesPage();
    }

    // Apre la sezione delle consegne degli studenti
    @FXML
    public void consegneClicked(ActionEvent actionEvent) throws IOException {
        sh.setConsegnePage();
    }

    // Effettua il logout e torna alla pagina di login
    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        sh.setLoginPage();
    }

}
