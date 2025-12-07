package application.controller;

import application.Database;
import application.MessageDebug;
import application.SceneHandler;
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
    private ImageView logoView;
    @FXML
    private TextField userField;
    @FXML
    private PasswordField passwordField;
    @FXML
    private TextField passwordVisibleField;
    @FXML
    private FontAwesomeIcon toggleIconPassword;

    // Metodo per navigare alla pagina di registrazione
    @FXML
    private void registrationClicked() throws IOException {
        SceneHandler.getInstance().setRegistrationPage();
    }

    // Metodo per effettuare il login
    @FXML
    private void loginClicked() throws IOException {
        String username = userField.getText();
        String password = passwordField.getText();


        // Controllo che i campi non siano vuoti
        if (username.isEmpty() || password.isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
        }
        // Controllo che le credenziali siano valide
        else if (Database.getInstance().validateCredentials(username, password)) {

            // Naviga alla home page in base al tipo di utente
            String typeUser = Database.getInstance().getTypeUser(username);
            if (typeUser.equals("studente")) {
                SceneHandler.getInstance().setStudentHomePage(username);
            } else if (typeUser.equals("professore")) {
                SceneHandler.getInstance().setProfessorHomePage(username);
            }
        } else {
            // Mostro un messaggio di errore se le credenziali non sono valide
            SceneHandler.getInstance().showWarning(MessageDebug.CREEDENTIALS_NOT_VALID);
        }
    }

    public void initialize() {
        String imagePath = getClass().getResource("/icon/logo.png").toExternalForm();
        logoView.setImage(new Image(imagePath));

        // aggiungiamo i listener per la sincronizzazione dei campi password
        addListeners();

        // settiamo il focus sul campo username
        userField.requestFocus();
    }

    // Metodo per aggiungere i listener ai campi password
    private void addListeners() {
        passwordField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordVisibleField.isFocused()) {
                passwordVisibleField.setText(newValue);
            }
        });

        passwordVisibleField.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!passwordField.isFocused()) {
                passwordField.setText(newValue);
            }
        });

        //aggiungiamo un listener per il tasto invio della tastiera per il login
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

    // Metodo per mostrare/nascondere la password
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
