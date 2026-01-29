package application.controller;

import application.persistence.Database;
import application.utility.MessageDebug;
import application.view.SceneHandler;
import de.jensd.fx.glyphs.fontawesome.FontAwesomeIcon;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;

public class LoginController {

    @FXML
    private ImageView logoView;               // Logo dell'applicazione
    @FXML
    private TextField userField;              // Campo per l'inserimento dell'username
    @FXML
    private PasswordField passwordField;      // Campo password nascosto
    @FXML
    private TextField passwordVisibleField;   // Campo password visibile (toggle)
    @FXML
    private FontAwesomeIcon toggleIconPassword; // Icona per mostra/nascondi password


    private SceneHandler sh;
    private Database db;


    // Inizializza la pagina di login
    public void initialize() {

        sh = SceneHandler.getInstance();
        db = Database.getInstance();

        caricaLogo();

        // Sincronizza i campi password visibile/nascosto
        addListeners();

        // Imposta il focus sul campo username
        userField.requestFocus();
    }

    private void caricaLogo() {
        // Carica e imposta il logo
        String imagePath = getClass().getResource("/icon/logo.png").toExternalForm();
        logoView.setImage(new Image(imagePath));
    }


    // Metodo per navigare alla pagina di registrazione
    @FXML
    private void registrationClicked() throws IOException {
        sh.setRegistrationPage();
    }

    // Metodo per effettuare il login
    @FXML
    private void loginClicked() throws IOException {
        String username = userField.getText();
        String password = passwordField.getText();

        // Controllo che i campi non siano vuoti
        if (username.isEmpty() || password.isEmpty()) {
            sh.showWarning(MessageDebug.CAMPS_NOT_EMPTY);
        }
        // Controllo che le credenziali siano valide
        else if (db.validateCredentials(username, password)) {

            // Naviga alla home page in base al tipo di utente
            String typeUser = db.getTypeUser(username);
            if (typeUser.equals("studente")) {
                sh.setStudentHomePage(username);
            } else if (typeUser.equals("professore")) {
                sh.setProfessorHomePage(username);
            }
        } else {
            // Messaggio se credenziali non valide
            sh.showWarning(MessageDebug.CREEDENTIALS_NOT_VALID);
        }
    }


    // Aggiunge i listener per sincronizzare i campi password e gestire il tasto ENTER
    private void addListeners() {
        // Aggiorna il campo visibile quando cambia quello nascosto
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordVisibleField.isFocused()) {
                passwordVisibleField.setText(newValue);
            }
        });

        // Aggiorna il campo nascosto quando cambia quello visibile
        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newValue);
            }
        });

        // Login tramite tasto ENTER
        passwordField.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case ENTER:
                    try {
                        loginClicked();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    break;
            }
        });
    }

    // Mostra o nasconde la password in base allo stato corrente
    @FXML
    public void togglePasswordVisibility(MouseEvent mouseEvent) {
        if (passwordField.isVisible()) {
            passwordField.setVisible(false);
            passwordVisibleField.setVisible(true);
            passwordVisibleField.setText(passwordField.getText());
            toggleIconPassword.setIconName("EYE_SLASH");
        } else {
            passwordField.setVisible(true);
            passwordVisibleField.setVisible(false);
            passwordField.setText(passwordVisibleField.getText());
            toggleIconPassword.setIconName("EYE");
        }
    }
}