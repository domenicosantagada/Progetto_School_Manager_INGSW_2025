package application.controller.studente;

import application.persistence.Database;
import application.view.SceneHandler;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.io.IOException;

public class HomeStudentController {

    @FXML
    private ImageView logoView;       // Logo dell'istituto
    @FXML
    private Label studentInfo;        // Nome completo dello studente
    @FXML
    private Label classeStudente;     // Classe dello studente


    private SceneHandler sh;
    private Database db;
    private String usernameStudente, classe;


    // Inizializza la home dello studente: carica logo, nome e classe
    public void initialize() {

        sh = SceneHandler.getInstance();
        db = Database.getInstance();

        usernameStudente = sh.getUsername();
        classe = db.getClasseUser(usernameStudente);

        caricaLogo();
        caricaInfoStudente();


    }

    private void caricaLogo() {
        // Carica il logo dell'istituto
        String imagePath = getClass().getResource("/icon/logo1.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
        logoView.setSmooth(true);
    }

    private void caricaInfoStudente() {
        // Imposta il nome completo dello studente
        String studentInfoText = db.getFullName(usernameStudente);
        studentInfo.setText(studentInfoText.toUpperCase());

        // Imposta la classe dello studente
        classeStudente.setText(classe.toUpperCase());
    }


    // Mostra la pagina delle performance dello studente
    @FXML
    public void performanceClicked(ActionEvent actionEvent) throws IOException {
        sh.setAndamentoPage();
    }

    // Mostra la pagina dei compiti assegnati
    @FXML
    public void assignmentClicked(ActionEvent actionEvent) throws IOException {
        sh.setCompitiPage();
    }

    // Mostra la pagina delle note disciplinari
    @FXML
    public void notesButtonClicked(ActionEvent actionEvent) throws IOException {
        sh.setNotePage();
    }

    // Mostra la pagina delle assenze dello studente
    @FXML
    public void assenzeClicked(ActionEvent actionEvent) throws IOException {
        sh.setAssenzeStudentePage();
    }

    // Effettua il logout e ritorna alla pagina di login
    @FXML
    public void logoutClicked(ActionEvent actionEvent) throws IOException {
        sh.setLoginPage();
    }
    
}