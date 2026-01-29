package application.controller.studente;

import  application.model.Assenza;
import application.observer.DatabaseObserver;
import application.persistence.Database;
import application.persistence.DatabaseEvent;
import application.view.SceneHandler;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.util.Comparator;
import java.util.List;

public class AssenzeStudenteController implements DatabaseObserver {

    @FXML
    private BorderPane mainPane, giustificaPane;
    @FXML
    private TableView<Assenza> assenzeTable;
    @FXML
    private TableColumn<Assenza, String> dataColumn, motivazioneColumn, giustificataColumn;
    @FXML
    private TextArea motivazioneArea;
    @FXML
    private Button giustificaButton;

    private final ObservableList<Assenza> assenzeList = FXCollections.observableArrayList();

    // Inizializza il controller: registra observer, configura colonne e carica dati
    public void initialize() {
        Database.getInstance().attach(this);

        dataColumn.setCellValueFactory(cellData -> {
            Assenza a = cellData.getValue();
            return new SimpleStringProperty(a.giorno() + "/" + a.mese() + "/" + a.anno());
        });
        motivazioneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().motivazione()));
        giustificataColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().giustificata() ? "Sì" : "No")
        );

        loadData();
    }

    // Carica le assenze dello studente e ordina dalla meno recente alla più recente
    private void loadData() {
        String username = SceneHandler.getInstance().getUsername();
        List<Assenza> assenze = Database.getInstance().getAssenzeStudente(username);

        assenze.sort(Comparator.comparing(Assenza::anno)
                .thenComparing(Assenza::mese)
                .thenComparing(Assenza::giorno));

        assenzeList.setAll(assenze);
        assenzeTable.setItems(assenzeList);
    }

    // Torna alla home dello studente e rimuove l'observer
    @FXML
    public void backClicked() throws IOException {
        Database.getInstance().detach(this);
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

    // Mostra il pannello per giustificare un'assenza selezionata
    @FXML
    public void giustificaClicked() {
        Assenza selected = assenzeTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            SceneHandler.getInstance().showWarning("Seleziona un'assenza da giustificare.");
            return;
        }
        if (selected.giustificata()) {
            SceneHandler.getInstance().showInformation("Questa assenza è già stata giustificata.");
            return;
        }

        motivazioneArea.clear();
        mainPane.setEffect(new GaussianBlur());
        mainPane.setDisable(true);
        giustificaPane.setVisible(true);
    }

    // Conferma la giustificazione dell'assenza selezionata
    @FXML
    public void confermaGiustificaClicked() {
        Assenza selected = assenzeTable.getSelectionModel().getSelectedItem();
        String motivazione = motivazioneArea.getText().trim();

        if (motivazione.isEmpty()) {
            SceneHandler.getInstance().showWarning("Inserisci una motivazione.");
            return;
        }

        Database.getInstance().justifyAssenza(selected, motivazione);

        annullaGiustificaClicked();
    }

    // Chiude il pannello di giustificazione
    @FXML
    public void annullaGiustificaClicked() {
        giustificaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Aggiorna la tabella quando i dati nel database cambiano
    @Override
    public void update(DatabaseEvent event) {
        switch (event.type()) {
            case ASSENZA_AGGIUNTA:
            case ASSENZA_GIUSTIFICATA:
            case ASSENZA_ELIMINATA:
                System.out.println("Notifica ricevuta: " + event.type() + ". Aggiornamento tabella in corso...");
                javafx.application.Platform.runLater(this::loadData);
                break;
            default:
                break;
        }
    }
}
