package application.controller.studente;

import application.dao.CompitiDAO;
import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import application.persistence.Database;
import application.view.SceneHandler;
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

    @FXML
    private BorderPane mainPane, caricaElaboratoPane;

    @FXML
    private Label materiaLabeld, argomentoLabel, classeLabel;

    @FXML
    private VBox compitiContainer, elaboratiContainer;

    @FXML
    private TextArea commentoArea;

    private String studente;
    private String classe;
    private List<CompitoAssegnato> compiti;
    private CompitoAssegnato selectedCompito;
    private File selectedFile;

    // Inizializza controller: imposta classe, carica compiti e nasconde pannello elaborato
    @FXML
    public void initialize() {
        caricaElaboratoPane.setVisible(false);
        studente = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(studente);
        classeLabel.setText(classe.toUpperCase());

        compiti = Database.getInstance().getCompitiClasse(classe);
        visualizzaCompiti();
    }

    // Torna alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(studente);
    }

    // Visualizza i compiti assegnati nel container
    private void visualizzaCompiti() {
        compitiContainer.getChildren().clear();
        if (compiti != null && !compiti.isEmpty()) {
            for (CompitoAssegnato comp : compiti) {
                generaLabel(comp);
            }
        }
    }

    // Genera il BorderPane cliccabile per ogni compito
    private void generaLabel(CompitoAssegnato comp) {
        BorderPane newBorderPane = new BorderPane();

        Label materia = new Label(comp.materia().toUpperCase());
        Label message = new Label(comp.descrizione());
        Label date = new Label(comp.data());

        newBorderPane.setTop(materia);
        newBorderPane.setCenter(message);
        newBorderPane.setBottom(date);
        newBorderPane.setAlignment(materia, Pos.CENTER);
        newBorderPane.setAlignment(message, Pos.CENTER);
        newBorderPane.setAlignment(date, Pos.CENTER);

        newBorderPane.getStyleClass().add("compitiPane");
        newBorderPane.setStyle("-fx-cursor: hand;");

        materia.getStyleClass().add("materiaLabel");
        message.getStyleClass().add("messageLabel");
        date.getStyleClass().add("dateLabel");

        // Evento click: mostra pannello caricamento elaborato
        newBorderPane.setOnMouseClicked(event -> {
            selectedCompito = comp;
            selectedFile = null;
            commentoArea.clear();

            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
            caricaElaboratoPane.setVisible(true);
            caricaElaboratoPane.requestFocus();

            materiaLabeld.setText(comp.materia().toUpperCase());
            argomentoLabel.setText(comp.descrizione());

            caricaElaboratiEsistenti(comp);
        });

        compitiContainer.getChildren().add(newBorderPane);
    }

    // Carica gli elaborati già inviati dallo studente per il compito selezionato
    private void caricaElaboratiEsistenti(CompitoAssegnato comp) {
        elaboratiContainer.getChildren().clear();
        CompitiDAO compitiDAO = new CompitiDAO();
        List<ElaboratoCaricato> elaborati = compitiDAO.getElaboratiCompito(comp.id());

        elaborati.stream()
                .filter(e -> e.studente().equals(studente))
                .forEach(elaborato -> {
                    BorderPane elPane = new BorderPane();

                    Label info = new Label(elaborato.data() + " - " +
                            (elaborato.commento() != null && !elaborato.commento().isEmpty() ? elaborato.commento() : "Nessun commento"));
                    info.setMaxWidth(300);
                    info.setWrapText(true);

                    javafx.scene.control.Button scaricaBtn = new javafx.scene.control.Button("Scarica");
                    scaricaBtn.setOnAction(e -> scaricaPDF(elaborato));

                    javafx.scene.control.Button eliminaBtn = new javafx.scene.control.Button("Elimina");
                    eliminaBtn.setStyle("-fx-background-color: #ffcccc;");
                    eliminaBtn.setOnAction(e -> {
                        if (Database.getInstance().deleteElaborato(elaborato.id())) {
                            SceneHandler.getInstance().showInformation("Elaborato eliminato.");
                            caricaElaboratiEsistenti(comp);
                        } else {
                            SceneHandler.getInstance().showWarning("Errore durante l'eliminazione.");
                        }
                    });

                    javafx.scene.layout.HBox buttons = new javafx.scene.layout.HBox(10, scaricaBtn, eliminaBtn);
                    buttons.setAlignment(Pos.CENTER_RIGHT);

                    elPane.setLeft(info);
                    elPane.setRight(buttons);
                    BorderPane.setAlignment(info, Pos.CENTER_LEFT);

                    elaboratiContainer.getChildren().add(elPane);
                });
    }

    // Scarica un elaborato selezionato in PDF
    private void scaricaPDF(ElaboratoCaricato elaborato) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva elaborato");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File PDF", "*.pdf"));
        fileChooser.setInitialFileName("MioElaborato.pdf");

        File file = fileChooser.showSaveDialog(mainPane.getScene().getWindow());
        if (file != null) {
            try (java.io.FileOutputStream fos = new java.io.FileOutputStream(file)) {
                fos.write(elaborato.file());
                SceneHandler.getInstance().showInformation("File salvato: " + file.getAbsolutePath());
            } catch (IOException e) {
                SceneHandler.getInstance().showWarning("Errore salvataggio: " + e.getMessage());
            }
        }
    }

    // Torna indietro dal pannello di caricamento elaborato
    @FXML
    public void backFromCaricaElaboratoClicked(MouseEvent mouseEvent) {
        caricaElaboratoPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
        selectedCompito = null;
        selectedFile = null;
    }

    // Seleziona il file PDF dell'elaborato
    @FXML
    public void caricaRisorsaClicked(ActionEvent actionEvent) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Scegli un file PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File PDF", "*.pdf"));

        File projectRoot = new File(System.getProperty("user.dir"));
        File initialDir = new File(projectRoot, "Esempio PDF esportati");
        if (!initialDir.exists()) initialDir.mkdirs();
        fileChooser.setInitialDirectory(initialDir);

        selectedFile = fileChooser.showOpenDialog(mainPane.getScene().getWindow());
        if (selectedFile != null) {
            System.out.println("File scelto: " + selectedFile.getAbsolutePath());
        }
    }

    // Invia l'elaborato al database
    @FXML
    public void inviaElaboratoClicked(ActionEvent actionEvent) {
        if (selectedFile == null) {
            SceneHandler.getInstance().showWarning("Devi selezionare un file PDF per l'elaborato.");
            return;
        }

        try {
            byte[] fileContent = Files.readAllBytes(selectedFile.toPath());
            String commento = commentoArea.getText();
            String data = LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy"));

            ElaboratoCaricato elaborato = new ElaboratoCaricato(selectedCompito, studente, data, commento, fileContent /*, false */);
            if (Database.getInstance().insertElaborato(elaborato)) {
                SceneHandler.getInstance().showInformation("Elaborato inviato con successo!");
                backFromCaricaElaboratoClicked(null);
            } else {
                SceneHandler.getInstance().showWarning("Errore durante l'invio dell'elaborato.");
            }

        } catch (IOException e) {
            SceneHandler.getInstance().showWarning("Errore durante la lettura del file: " + e.getMessage());
        }
    }
}
