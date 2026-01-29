package application.controller.prof;

import application.dao.CompitiDAO;
import application.model.CompitoAssegnato;
import application.model.ElaboratoCaricato;
import application.persistence.Database;
import application.view.SceneHandler;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
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
    CompitiDAO compitiDAO = new CompitiDAO();

    @FXML
    private VBox consegneContainer;

    @FXML
    private Label classeLabel;

    // Torna alla homepage del professore
    @FXML
    public void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    // Inizializza il controller e mostra i compiti
    @FXML
    public void initialize() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);
        classeLabel.setText(classe.toUpperCase());
        visualizzaCompiti();
    }

    // Mostra i compiti assegnati dal professore corrente
    private void visualizzaCompiti() {
        List<CompitoAssegnato> compiti = Database.getInstance().getCompitiClasse(classe);
        consegneContainer.getChildren().clear();

        for (CompitoAssegnato compito : compiti) {
            if (compito.prof().equals(prof)) {
                generaLabel(compito);
            }
        }
    }

    // Crea un pannello per un compito con gestione click e context menu
    private void generaLabel(CompitoAssegnato comp) {
        BorderPane newBorderPane = new BorderPane();
        Label materia = new Label(comp.materia().toUpperCase());
        Label message = new Label(comp.descrizione());
        Label date = new Label(comp.data());

        newBorderPane.setTop(materia);
        newBorderPane.setCenter(message);
        newBorderPane.setBottom(date);
        newBorderPane.setAlignment(materia, Pos.CENTER);
        newBorderPane.setAlignment(date, Pos.CENTER);
        newBorderPane.setAlignment(message, Pos.CENTER);

        newBorderPane.getStyleClass().add("compitiPane");
        newBorderPane.setStyle("-fx-cursor: hand;");

        materia.getStyleClass().add("materiaLabel");
        message.getStyleClass().add("messageLabel");
        date.getStyleClass().add("dateLabel");

        VBox elaboratiBox = new VBox();
        elaboratiBox.setSpacing(10);
        elaboratiBox.setVisible(false);
        elaboratiBox.setManaged(false);

        // Mostra/nasconde elaborati al click
        newBorderPane.setOnMouseClicked(event -> {
            if (event.getButton() == javafx.scene.input.MouseButton.SECONDARY) return;
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
        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Elimina Compito");
        deleteItem.setOnAction(e -> {
            if (Database.getInstance().hasElaboratiForCompito(comp.id())) {
                SceneHandler.getInstance().showWarning("Impossibile eliminare: ci sono elaborati consegnati.");
            } else {
                if (Database.getInstance().deleteCompito(comp.id())) {
                    SceneHandler.getInstance().showInformation("Compito eliminato correttamente.");
                    visualizzaCompiti();
                } else {
                    SceneHandler.getInstance().showWarning("Errore durante l'eliminazione del compito.");
                }
            }
        });
        contextMenu.getItems().add(deleteItem);
        newBorderPane.setOnContextMenuRequested(e -> contextMenu.show(newBorderPane, e.getScreenX(), e.getScreenY()));

        VBox container = new VBox(newBorderPane, elaboratiBox);
        consegneContainer.getChildren().add(container);
    }

    // Mostra gli elaborati caricati per un compito
    private void mostraElaborati(CompitoAssegnato compito, VBox container) {
        container.getChildren().clear();
        //List<ElaboratoCaricato> elaborati = compitiDAO.getNewElaboratiCompito(compito.id());
        List<ElaboratoCaricato> elaborati = compitiDAO.getElaboratiCompito(compito.id());

        if (elaborati.isEmpty()) {
            container.getChildren().add(new Label("Nessun elaborato consegnato."));
            return;
        }

        for (ElaboratoCaricato elaborato : elaborati) {
            BorderPane elPane = new BorderPane();
            elPane.setStyle("-fx-background-color: #f0f0f0; -fx-padding: 10; -fx-border-color: #ccc; -fx-border-radius: 5;");

            Label studenteLbl = new Label("Studente: " + Database.getInstance().getFullName(elaborato.studente()));
            Label dataLbl = new Label("Data: " + elaborato.data());
            Label commentoLbl = new Label("Commento: " + (elaborato.commento() != null ? elaborato.commento() : ""));
            /*
            Label approvatoLbl = new Label("Approvato: " +  (elaborato.approvato() ? "SI" : "NO"));
            Button approvaBtn = new Button("Approva");
            approvaBtn.setOnAction(e -> {
                compitiDAO.updateElaborato(elaborato.id());
                approvatoLbl.setText("Approvato: SI");
                approvaBtn.setVisible(false);
                approvaBtn.setManaged(true);
            });
             */

            VBox infoBox = new VBox(studenteLbl, dataLbl, commentoLbl /*, approvatoLbl*/);
            //if(!elaborato.approvato()) infoBox.getChildren().add(approvaBtn);
            elPane.setLeft(infoBox);

            Label downloadLbl = new Label("Scarica PDF");
            downloadLbl.setStyle("-fx-text-fill: blue; -fx-underline: true; -fx-cursor: hand;");
            downloadLbl.setOnMouseClicked(e -> scaricaPDF(elaborato));

            elPane.setRight(downloadLbl);
            container.getChildren().add(elPane);
        }
    }

    // Scarica il PDF dell'elaborato selezionato
    private void scaricaPDF(ElaboratoCaricato elaborato) {
        String nomeStudente = Database.getInstance().getFullName(elaborato.studente());
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva Elaborato PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("File PDF", "*.pdf"));
        fileChooser.setInitialFileName("Elaborato " + nomeStudente + ".pdf");

        File file = fileChooser.showSaveDialog(consegneContainer.getScene().getWindow());
        if (file != null) {
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
