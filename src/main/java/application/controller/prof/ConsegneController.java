package application.controller.prof;

import application.Database;
import application.SceneHandler;
import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;

public class ConsegneController {

    private String prof;

    private String classe;


    @FXML
    private VBox consegneContainer;

    @FXML
    private Label classeLabel;

    // Metodo per tornare alla home dello studente
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    @FXML
    public void initialize() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);

        classeLabel.setText(classe.toUpperCase());

        visualizzaCompiti();
    }

    // Metodo per visualizzare i compiti assegnati
    private void visualizzaCompiti() {
        List<CompitoAssegnato> compiti = Database.getInstance().getCompitiClasse(classe);
        consegneContainer.getChildren().clear();

        for (CompitoAssegnato compito : compiti) {
            // Filtra per mostrare solo i compiti assegnati dal professore corrente
            if (compito.prof().equals(prof)) {
                generaLabel(compito);
            }
        }
    }

    private void generaLabel(CompitoAssegnato comp) {
        BorderPane newBorderPane = new BorderPane();
        Label materia = new Label(comp.materia().toUpperCase());
        Label message = new Label(comp.descrizione());
        Label date = new Label(comp.data());

        newBorderPane.setTop(materia);
        newBorderPane.setCenter(message);
        newBorderPane.setBottom(date);
        newBorderPane.setAlignment(newBorderPane.getTop(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getBottom(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getCenter(), Pos.CENTER);

        // Aggiungo stile al BorderPane
        newBorderPane.getStyleClass().add("compitiPane");
        newBorderPane.setStyle("-fx-cursor: hand;");

        // Stili Label
        materia.getStyleClass().add("materiaLabel");
        message.getStyleClass().add("messageLabel");
        date.getStyleClass().add("dateLabel");

        // Container per gli elaborati (inizialmente nascosto o vuoto)
        VBox elaboratiBox = new VBox();
        elaboratiBox.setSpacing(10);
        elaboratiBox.setVisible(false);
        elaboratiBox.setManaged(false); // Non occupa spazio se non visibile

        // Quando si clicca sul compito, mostra/nasconde gli elaborati
        newBorderPane.setOnMouseClicked(event -> {
            // Se il click è stato fatto col tasto destro, ignoriamo l'espansione (gestita dal ContextMenu)
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) {
                return;
            }
            if (!elaboratiBox.isVisible()) {
                mostraElaborati(comp, elaboratiBox);
                elaboratiBox.setVisible(true);
                elaboratiBox.setManaged(true);
            } else {
                elaboratiBox.setVisible(false);
                elaboratiBox.setManaged(false);
            }
        });

        // Context Menu per eliminare il compito
        ContextMenu contextMenu = new javafx.scene.control.ContextMenu();
        MenuItem deleteItem = new javafx.scene.control.MenuItem("Elimina Compito");
        deleteItem.setOnAction(e -> {
            // Controlla se ci sono elaborati per questo compito
            if (Database.getInstance().hasElaboratiForCompito(comp.id())) {
                SceneHandler.getInstance().showWarning("Impossibile eliminare: ci sono elaborati consegnati.");
            } else {
                // Procedi con l'eliminazione
                if (Database.getInstance().deleteCompito(comp.id())) {
                    SceneHandler.getInstance().showInformation("Compito eliminato correttamente.");
                    // Aggiorna la vista
                    visualizzaCompiti();
                } else {
                    SceneHandler.getInstance().showWarning("Errore durante l'eliminazione del compito.");
                }
            }
        });
        contextMenu.getItems().add(deleteItem);

        // Associa il ContextMenu al pannello del compito
        newBorderPane.setOnContextMenuRequested(e -> contextMenu.show(newBorderPane, e.getScreenX(), e.getScreenY()));

        // Avvolge il compito e la lista elaborati in un VBox
        VBox container = new VBox(newBorderPane, elaboratiBox);
        consegneContainer.getChildren().add(container);
    }

    // Metodo per mostrare gli elaborati caricati per un compito specifico
    private void mostraElaborati(CompitoAssegnato compito, VBox container) {
        // Pulisce il contenuto precedente e carica gli elaborati dal database
        container.getChildren().clear();
        List<ElaboratoCaricato> elaborati = Database.getInstance().getElaboratiCompito(compito.id());

        if (elaborati.isEmpty()) {
            container.getChildren().add(new Label("Nessun elaborato consegnato."));
            return;
        }

        // Crea una sezione per ogni elaborato
        for (ElaboratoCaricato elaborato : elaborati) {
            BorderPane elPane = new BorderPane();
            elPane.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

            Label studenteLbl = new Label("Studente: " + Database.getInstance().getFullName(elaborato.studente()));
            Label dataLbl = new Label("Data: " + elaborato.data());
            Label commentoLbl = new Label("Commento: " + (elaborato.commento() != null ? elaborato.commento() : ""));

            VBox infoBox = new VBox(studenteLbl, dataLbl, commentoLbl);
            elPane.setLeft(infoBox);

            Label downloadLbl = new Label("Scarica PDF");
            downloadLbl.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
            downloadLbl.setOnMouseClicked(e -> scaricaPDF(elaborato));

            elPane.setRight(downloadLbl);

            container.getChildren().add(elPane);
        }
    }

    // Metodo per scaricare l'elaborato in formato PDF
    private void scaricaPDF(ElaboratoCaricato elaborato) {
        String nomeStudente = Database.getInstance().getFullName(elaborato.studente());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Elaborato PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File PDF", "*.pdf"));
        fileChooser.setInitialFileName("Elaborato " + nomeStudente + ".pdf");

        File file = fileChooser.showSaveDialog(consegneContainer.getScene().getWindow());
        if (file != null) {
            // Scrive i byte del file PDF selezionato
            try (FileOutputStream fos = new FileOutputStream(file)) {
                fos.write(elaborato.file());
                SceneHandler.getInstance().showInformation("File salvato correttamente in: " + file.getAbsolutePath());
            } catch (IOException e) {
                SceneHandler.getInstance().showWarning("Errore durante il salvataggio del file: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
}