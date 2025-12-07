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

    // Costanti per distinguere i tipi di utente
    public static final String PROFTYPE = "professore";
    public static final String STUDENTTYPE = "studente";

    @FXML
    private ChoiceBox materiaChoiceBox; // lista materie, visibile solo per i prof
    @FXML
    private Button registerButton; // pulsante di registrazione
    @FXML
    private Label materiaLabel; // label della materia (solo prof)
    @FXML
    private TextField codiceIscrizione; // codice di iscrizione (identifica classe o ruolo)
    @FXML
    private TextField surnameField; // cognome
    @FXML
    private PasswordField repeatPasswordField; // conferma password
    @FXML
    private TextField nameField; // nome
    @FXML
    private DatePicker datePicker; // data di nascita
    @FXML
    private PasswordField passwordField; // password
    @FXML
    private TextField usernameField; // username
    @FXML
    private ImageView logoView; // immagine del logo

    @FXML
    public void registerClicked(ActionEvent actionEvent) throws IOException {
        // Lettura dei campi utente
        String username = this.usernameField.getText();
        String nome = this.nameField.getText();
        String cognome = this.surnameField.getText();
        String dataNascita = "";

        // Formattazione della data (se inserita)
        if (datePicker.getValue() != null) {
            dataNascita = this.datePicker.getValue().format(DateTimeFormatter.ofPattern("dd-MM-yyyy"));
        }

        // Codice iscrizione e materia (per i professori)
        String codiceIscrizione = this.codiceIscrizione.getText();
        String materia = this.materiaChoiceBox.getValue() == null ? "" : this.materiaChoiceBox.getValue().toString();

        // Password e hashing
        String password = this.passwordField.getText();
        String hashedPassword = BCryptService.hashPassword(password);

        // Controlli preliminari sui campi
        if (username.isEmpty() || nome.isEmpty() || cognome.isEmpty() || dataNascita.isEmpty()
                || codiceIscrizione.isEmpty() || password.isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
        } else if (usernameUtilizzato(username)) { // Username già presente
            SceneHandler.getInstance().showWarning(MessageDebug.USERNAME_NOT_VALID);
        } else if (datePicker.getValue().isAfter(LocalDate.now())) { // Data impossibile
            SceneHandler.getInstance().showWarning(MessageDebug.DATE_NOT_VALID);
        } else if (!password.equals(repeatPasswordField.getText())) { // Le password non combaciano
            SceneHandler.getInstance().showWarning(MessageDebug.PASSWORD_NOT_MATCH);
        } else if (password.length() < 4) { // Password troppo corta
            SceneHandler.getInstance().showWarning(MessageDebug.PASSWORD_NOT_VALID);
        } else {
            // Creazione oggetto utente generico
            User user = new User(username, nome.toUpperCase(), cognome.toUpperCase(), hashedPassword, dataNascita);

            // Recupero tipo (prof o studente) dal codice di iscrizione
            TipologiaClasse tipologiaUtente = Database.getInstance().getTipologiaUtente(codiceIscrizione);

            if (tipologiaUtente == null) { // Codice non valido
                SceneHandler.getInstance().showWarning(MessageDebug.CODE_ERROR);
            } else if (tipologiaUtente.tipologia().equals("studente")) {
                // Creazione studente
                Studente studente = new Studente(user, tipologiaUtente.classe());
                if (Database.getInstance().insertStudente(studente)) {
                    SceneHandler.getInstance().showInformation(MessageDebug.REGISTRATION_OK);
                    SceneHandler.getInstance().setLoginPage();
                }
            } else if (tipologiaUtente.tipologia().equals("professore")) {
                // Registrazione professore richiede anche materia
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

    // Controlla se lo username è già usato
    private boolean usernameUtilizzato(String username) {
        return Database.getInstance().usernameUtilizzato(username);
    }

    // Verifica validità del codice di iscrizione
    private boolean codiceIscrizioneValido(String codiceIscrizione) {
        return Database.getInstance().codiceIscrizioneValido(codiceIscrizione);
    }

    // Recupera tipologia utente dal codice
    private String tipologiaUser(String codiceIscrizione) {
        return Database.getInstance().tipologiaUser(codiceIscrizione);
    }

    public void initialize() {
        // Imposta immagine del logo
        String imagePath = getClass().getResource("/icon/logo.png").toExternalForm();
        logoView.setImage(new Image(imagePath));

        // Aggiunge i listener per la validazione in tempo reale
        addListeners();

        // Nasconde i campi relativi ai professori
        materiaChoiceBox.setVisible(false);
        materiaLabel.setVisible(false);

        // Riempie la choice box delle materie
        setMaterieChoiceBox();
    }

    // Inserisce nella choiceBox le materie prelevate dal DB
    private void setMaterieChoiceBox() {
        List<String> materie = Database.getInstance().getAllMaterieIstituto();
        materiaChoiceBox.getItems().addAll(materie);
    }

    // Listener per validazione live dei campi
    private void addListeners() {

        // Listener sul campo username
        usernameField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                // Controlli sullo username
                if (newValue == null || newValue.isBlank()) {
                    usernameField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!usernameUtilizzato(newValue)) {
                    usernameField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                } else {
                    usernameField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                }
            }
        });

        // Listener sul codice di iscrizione
        codiceIscrizione.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null || newValue.isBlank()) {
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!codiceIscrizioneValido(newValue)) {
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    codiceIscrizione.styleProperty().set(MessageDebug.CHECK_OK_COLOR);

                    // Se il codice indica un professore, mostra campo materia
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

        // Listener sulla password
        passwordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null || newValue.isBlank()) {
                    passwordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (newValue.length() < 4) {
                    passwordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    passwordField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                }
            }
        });

        // Listener sulla ripetizione password
        repeatPasswordField.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {

                if (newValue == null || newValue.isBlank()) {
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else if (!newValue.equals(passwordField.getText())) {
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_NOT_OK_COLOR);
                } else {
                    repeatPasswordField.styleProperty().set(MessageDebug.CHECK_OK_COLOR);
                }
            }
        });
    }

    // Torna alla pagina di login
    @FXML
    public void backButtonClicked(MouseEvent mouseEvent) throws IOException {
        // Torna alla pagina login
        SceneHandler.getInstance().setLoginPage();
    }
}