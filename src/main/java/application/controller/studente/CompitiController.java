package application.controller.studente;

import application.Database;
import application.SceneHandler;
import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class CompitiController {

    public BorderPane caricaElaboratoPane;
    public BorderPane mainPane;
    public Label materiaLabeld;
    public Label argomentoLabel;
    public TextArea commentoArea;
    private String studente;
    private String classe;
    private List<CompitoAssegnato> compiti = null;
    private CompitoAssegnato selectedCompito; // Per tenere traccia del compito selezionato
    private File selectedFile; // Per tenere traccia del file selezionato

    @FXML
    private VBox compitiContainer;
    @FXML
    private Label classeLabel;

    // Metodo per tornare alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        caricaElaboratoPane.setVisible(false);
        studente = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(studente);
        compiti = Database.getInstance().getCompitiClasse(classe);
        classeLabel.setText(classe.toUpperCase());

        visualizzaCompiti();
    }

    // Metodo per visualizzare i compiti assegnati
    private void visualizzaCompiti() {
        // Controllo se ci sono compiti assegnati
        if (compiti != null && !compiti.isEmpty()) {

            // Pulisco il container dei compiti
            compitiContainer.getChildren().clear();

            // Genero le etichette per ogni compito
            for (CompitoAssegnato comp : compiti)
                generaLabel(comp);
        }
    }

    private void generaLabel(CompitoAssegnato comp) {
        BorderPane newBorderPane = new BorderPane();
        Label materia = new Label();
        materia.setText(comp.materia().toUpperCase());
        Label message = new Label();
        message.setText(comp.descrizione());
        Label date = new Label();
        date.setText(comp.data());

        newBorderPane.setTop(materia);
        newBorderPane.setCenter(message);
        newBorderPane.setBottom(date);
        newBorderPane.setAlignment(newBorderPane.getTop(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getBottom(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getCenter(), Pos.CENTER);

        // Aggiungo stile al BorderPane
        newBorderPane.getStyleClass().add("compitiPane");

        // Aggiungo stile per il cursore (opzionale, per far capire che è cliccabile)
        newBorderPane.setStyle("-fx-cursor: hand;");

        // Aggiungo stile al label nel top
        materia.getStyleClass().add("materiaLabel");
        // Aggiungo stile al label nel center
        message.getStyleClass().add("messageLabel");
        // Aggiungo stile al label nel bottom
        date.getStyleClass().add("dateLabel");

        // [MODIFICA] Aggiungo l'evento di click per stampare le info sul terminale
        newBorderPane.setOnMouseClicked(event -> {
            System.out.println("Hai cliccato sul compito di: " + comp.materia());
            System.out.println("Professore: " + comp.prof());
            System.out.println("Descrizione: " + comp.descrizione());
            System.out.println("Data inserimento: " + comp.data());
            System.out.println("------------------------------");

            selectedCompito = comp; // Salvo il compito selezionato
            selectedFile = null; // Resetta il file selezionato
            commentoArea.setText(""); // Resetta il commento

            // Mostro il pannello di caricamento elaborato
            mainPane.setDisable(true);
            caricaElaboratoPane.setVisible(true);
            mainPane.setEffect(new GaussianBlur());
            caricaElaboratoPane.requestFocus();

            // Imposto le etichette nel pannello di caricamento elaborato
            materiaLabeld.setText(comp.materia().toUpperCase());
            argomentoLabel.setText(comp.descrizione());
        });

        // Aggiungo il BorderPane al container dei compiti
        compitiContainer.getChildren().add(newBorderPane);
    }

    // Metodo per tornare indietro dalla schermata di caricamento elaborato
    public void backFromCaricaElaboratoClicked(MouseEvent mouseEvent) {
        caricaElaboratoPane.setVisible(false);
        mainPane.setVisible(true);
        mainPane.setEffect(null);
        mainPane.setDisable(false);
        selectedCompito = null;
        selectedFile = null;
    }

    // Metodo per inviare l'elaborato
    public void inviaElaboratoClicked(ActionEvent actionEvent) {
        if (selectedFile == null) {
            SceneHandler.getInstance().showWarning("Devi selezionare un file PDF per l'elaborato.");
            return;
        }

        try {
            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
            String commento = commentoArea.getText();
            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            ElaboratoCaricato elaborato = new ElaboratoCaricato(
                    selectedCompito,
                    studente,
                    data,
                    commento,
                    fileContent
            );

            boolean success = Database.getInstance().insertElaborato(elaborato);
            if (success) {
                SceneHandler.getInstance().showInformation("Elaborato inviato con successo!");
                System.out.println("Elaborato inviato!");
                backFromCaricaElaboratoClicked(null);
            } else {
                SceneHandler.getInstance().showWarning("Errore durante l'invio dell'elaborato.");
            }

        } catch (IOException e) {
            e.printStackTrace();
            SceneHandler.getInstance().showWarning("Errore durante la lettura del file: " + e.getMessage());
        }
    }

    public void caricaRisorsaClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Scegli un file PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File PDF", "*.pdf"));

        // Cartella iniziale: "Esempio PDF esportati" nella root del progetto
        File projectRoot = new File(System.getProperty("user.dir"));
        File initialDir = new File(projectRoot, "Esempio PDF esportati");
        if (!initialDir.exists()) {
            initialDir.mkdirs(); // crea la cartella se non esiste
        }
        fileChooser.setInitialDirectory(initialDir);

        // Ottieni lo stage dalla scena del mainPane
        selectedFile = fileChooser.showOpenDialog((javafx.stage.Window) mainPane.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("File scelto: " + selectedFile.getAbsolutePath());
        }
    }
}
