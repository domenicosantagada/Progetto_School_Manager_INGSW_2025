package application.controller.studente;

import application.Database;
import application.SceneHandler;
import application.model.Assenza;
import application.observer.DataObserver;
import javafx.application.Platform;
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

public class AssenzeStudenteController implements DataObserver {
    @FXML
    private BorderPane mainPane;
    @FXML
    private BorderPane giustificaPane;
    @FXML
    private TableView<Assenza> assenzeTable;
    @FXML
    private TableColumn<Assenza, String> dataColumn;
    @FXML
    private TableColumn<Assenza, String> motivazioneColumn;
    @FXML
    private TableColumn<Assenza, String> giustificataColumn;
    @FXML
    private TextArea motivazioneArea;
    @FXML
    private Button giustificaButton;

    private ObservableList<Assenza> assenzeList = FXCollections.observableArrayList();

    public void initialize() {
        // Registra questo controller come observer del database
        Database.getInstance().attach(this);

        // Configura le colonne della tabella
        dataColumn.setCellValueFactory(cellData -> {
            Assenza a = cellData.getValue();
            return new SimpleStringProperty(a.giorno() + "/" + a.mese() + "/" + a.anno());
        });
        motivazioneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().motivazione()));
        giustificataColumn.setCellValueFactory(cellData ->
                new SimpleStringProperty(cellData.getValue().giustificata() ? "Sì" : "No")
        );

        // Carica i dati
        loadData();
    }

    // Carica le assenze dello studente dalla base dati
    private void loadData() {
        String username = SceneHandler.getInstance().getUsername();
        // recupero le assenze dello studente
        List<Assenza> assenze = Database.getInstance().getAssenzeStudente(username);
        // Ordino dalla meno recente alla più recente
        assenze.sort(Comparator.comparing(Assenza::anno)
                .thenComparing(Assenza::mese)
                .thenComparing(Assenza::giorno));

        assenzeList.setAll(assenze);
        assenzeTable.setItems(assenzeList);
    }

    @FXML
    public void backClicked() throws IOException {
        Database.getInstance().detach(this);
        SceneHandler.getInstance().setStudentHomePage(SceneHandler.getInstance().getUsername());
    }

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

    @FXML
    public void confermaGiustificaClicked() {
        Assenza selected = assenzeTable.getSelectionModel().getSelectedItem();
        String motivazione = motivazioneArea.getText().trim();

        if (motivazione.isEmpty()) {
            SceneHandler.getInstance().showWarning("Inserisci una motivazione.");
            return;
        }

        Database.getInstance().justifyAssenza(selected, motivazione);

        // Chiudi il pannello
        annullaGiustificaClicked();
    }

    @FXML
    public void annullaGiustificaClicked() {
        giustificaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Metodo chiamato quando i dati nel database cambiano
    @Override
    public void update(Object event) {
        if (event instanceof String && "ASSENZA_GIUSTIFICATA".equals(event)) {
            // Aggiorna la tabella quando un'assenza viene giustificata (evento del pattern Observer)
            Platform.runLater(this::loadData);
        }
    }
}

