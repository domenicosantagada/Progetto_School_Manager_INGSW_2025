package application.controller;

import application.BCryptService;
import application.Database;
import application.MessageDebug;
import application.SceneHandler;
import application.model.Professore;
import application.model.Studente;
import application.model.TipologiaClasse;
import application.model.User;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class RegistrationController {
    public static final String PROFTYPE = "professore";
    public static final String STUDENTTYPE = "studente";

    @FXML
    private ChoiceBox materiaChoiceBox;

    @FXML
    private Button registerButton;

    @FXML
    private Label materiaLabel; // visibile solo se il tipo è professore

    @FXML
    private TextField codiceIscrizione; // codice di iscrizione (capiamo se è un prof o un alunno di una determinata classe)


    @FXML
    private TextField surnameField;

    @FXML
    private PasswordField repeatPasswordField;

    @FXML
    private TextField nameField;

    @FXML
    private DatePicker datePicker;

    @FXML
    private PasswordField passwordField;

    @FXML
    private TextField usernameField;

    @FXML
    private ImageView logoView;

    @FXML
    public void registerClicked(ActionEvent actionEvent) throws IOException {
        String username = this.usernameField.getText();
        String nome = this.nameField.getText();
        String cognome = this.surnameField.getText();
        String dataNascita = "";
        if (datePicker.getValue() != null) {
            dataNascita = this.datePicker.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }
        String codiceIscrizione = this.codiceIscrizione.getText();
        String materia = this.materiaChoiceBox.getValue() == null ? "" : this.materiaChoiceBox.getValue().toString();
        String password = this.passwordField.getText();
        String hashedPassword = BCryptService.hashPassword(password);

        if (username.isEmpty() || nome.isEmpty() || cognome.isEmpty() || dataNascita.isEmpty() || codiceIscrizione.isEmpty() || password.isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
        } else if (usernameUtilizzato(username)) {
            SceneHandler.getInstance().showWarning(MessageDebug.USERNAME_NOT_VALID);
        } else if (datePicker.getValue().isAfter(LocalDate.now())) {
            SceneHandler.getInstance().showWarning(MessageDebug.DATE_NOT_VALID);
        } else if (!password.equals(repeatPasswordField.getText())) {
            SceneHandler.getInstance().showWarning(MessageDebug.PASSWORD_NOT_MATCH);
        } else if (password.length() < 4) {
            SceneHandler.getInstance().showWarning(MessageDebug.PASSWORD_NOT_VALID);
        } else {
            User user = new User(username, nome.toUpperCase(), cognome.toUpperCase(), hashedPassword, dataNascita);
            TipologiaClasse tipologiaUtente = Database.getInstance().getTipologiaUtente(codiceIscrizione);

            if (tipologiaUtente == null) {
                SceneHandler.getInstance().showWarning(MessageDebug.CODE_ERROR);
            } else if (tipologiaUtente.tipologia().equals("studente")) {
                Studente studente = new Studente(user, tipologiaUtente.classe());
                if (Database.getInstance().insertStudente(studente)) {
                    SceneHandler.getInstance().showInformation(MessageDebug.REGISTRATION_OK);
                    SceneHandler.getInstance().setLoginPage();
                }
            } else if (tipologiaUtente.tipologia().equals("professore")) {
                if (materia.isEmpty()) {
                    SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
                } else {
                    Professore professore = new Professore(user, tipologiaUtente.classe(), materia);
                    if (Database.getInstance().insertProfessore(professore)) {
                        SceneHandler.getInstance().showInformation(MessageDebug.REGISTRATION_OK);
                        SceneHandler.getInstance().setLoginPage();
                    }
                }
            }
        }
    }

    private boolean usernameUtilizzato(String username) {
        return Database.getInstance().usernameUtilizzato(username);
    }

    private boolean codiceIscrizioneValido(String codiceIscrizione) {
        return Database.getInstance().codiceIscrizioneValido(codiceIscrizione);
    }

    private String tipologiaUser(String codiceIscrizione) {
        return Database.getInstance().tipologiaUser(codiceIscrizione);
    }

    public void initialize() {
        String imagePath = getClass().getResource("/icon/logo.png").toExternalForm();
        logoView.setImage(new Image(imagePath));

        addListeners();

        materiaChoiceBox.setVisible(false);
        materiaLabel.setVisible(false);

        setMaterieChoiceBox();
    }

    private void setMaterieChoiceBox() {
        List<String> materie = Database.getInstance().getAllMaterieIstituto();
        materiaChoiceBox.getItems().addAll(materie);
    }

    // il listener controlla se i campi sono vuoti o se la password è troppo corta e serve per cambiare il colore del campo
    // in tempo reale
    private void addListeners() {
        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null || newValue.isBlank()) {
                    // Caso 1: Campo vuoto o spazi
                    usernameField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!usernameUtilizzato(newValue)) {
                    // Caso 2: Username non utilizzato
                    usernameField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                } else {
                    // Caso 3: Username già utilizzato
                    usernameField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                }
            }
        });

        codiceIscrizione.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    // Caso 1: Campo vuoto o spazi
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!codiceIscrizioneValido(newValue)) {
                    // Caso 2: Codice non valido
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    // Caso 3: Codice valido
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                    if (tipologiaUser(newValue).equals(MessageDebug.PROF_TYPE)) {
                        materiaChoiceBox.setVisible(true);
                        materiaLabel.setVisible(true);
                    } else {
                        materiaChoiceBox.setVisible(false);
                        materiaLabel.setVisible(false);
                    }
                }
            }
        });

        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    // Caso 1: Campo vuoto o spazi
                    passwordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (newValue.length() < 4) {
                    // Caso 2: Password troppo corta
                    passwordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    // Caso 3: Password valida
                    passwordField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                }
            }
        });

        repeatPasswordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                if (newValue == null || newValue.isBlank()) {
                    // Caso 1: Campo vuoto o spazi
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!newValue.equals(passwordField.getText())) {
                    // Caso 2: Password non corrispondente
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    // Caso 3: Password corrispondente
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                }
            }
        });
    }


    @FXML
    public void backButtonClicked(MouseEvent mouseEvent) throws IOException {
        SceneHandler.getInstance().setLoginPage();
    }
}