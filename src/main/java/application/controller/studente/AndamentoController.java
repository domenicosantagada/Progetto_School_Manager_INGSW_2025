package application.controller.studente;

import application.Database;
import application.SceneHandler;
import application.export.ExportContext;
import application.export.PDFExportStrategy;
import application.model.ValutazioneStudente;
import application.observer.DataObserver;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class AndamentoController implements DataObserver {

    // Elenco delle materie dell'istituto
    private List<String> materie = new ArrayList<>();

    // Username dello studente loggato
    private String studente;

    // Lista dei voti dello studente
    private List<ValutazioneStudente> voti = new ArrayList<>();

    @FXML
    private VBox listaVotiVBox;          // Contenitore delle card dei voti
    @FXML
    private BarChart<String, Number> andamentoChart;   // Grafico andamento voti
    @FXML
    private CategoryAxis materieX;       // Asse X del grafico (materie)
    @FXML
    private Label votiInAttesaLabel;     // Numero voti mancanti
    @FXML
    private Label insufficienzeLabel;    // Numero voti insufficienti
    @FXML
    private Label sufficienzeLabel;      // Numero voti sufficienti
    @FXML
    private Label mediaVoti;             // Media dei voti dello studente
    @FXML
    private Label nominativoStudente;    // Nome e cognome dello studente
    @FXML
    private Label classeStudente;        // Classe dello studente


    // Metodo per tornare alla home dello studente
    @FXML
    private void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(studente);
    }

    @FXML
    private void exportPDF(MouseEvent event) {
        // Esporta i voti dello studente in PDF tramite Strategy Pattern
        ExportContext exportContext = ExportContext.getInstance();
        exportContext.setStrategy(new PDFExportStrategy());
        // se volessi esportare in altri formati, aggiungi altre strategie qui
        // esempio: exportContext.setStrategy(new CSVExportStrategy());
        exportContext.exportValutazione(voti);
    }

    @FXML
    public void initialize() {
        // Carica username dello studente loggato
        studente = SceneHandler.getInstance().getUsername();

        // Registra il controller come observer per le modifiche ai dati dello studente
        Database.getInstance().attach(this);

        // Carica tutte le materie dell'istituto
        materie = Database.getInstance().getAllMaterieIstituto();
        materieX.setCategories(FXCollections.observableArrayList(materie));

        // Carica voti dello studente
        voti = Database.getInstance().getVotiStudente(studente);

        // Imposta informazioni dello studente nella UI
        nominativoStudente.setText(Database.getInstance().getFullName(studente).toUpperCase());
        classeStudente.setText(Database.getInstance().getClasseUser(studente).toUpperCase());

        // Aggiorna tutte le sezioni dell'interfaccia
        updateChart(voti);
        updateListVoti(voti);
        updateRiepilogo(voti);
    }

    // Aggiorna i contatori nel riepilogo
    private void updateRiepilogo(List<ValutazioneStudente> voti) {
        // Inizializza contatori
        int insufficienze = 0;
        int sufficienze = 0;
        int votiInAttesa = 0;
        int sommaVoti = 0;
        int votiTotali = 0;

        // Analizza la valutazione di ogni materia
        for (ValutazioneStudente voto : voti) {
            if (voto.voto() == 0) {
                votiInAttesa++;  // 0 = voto non ancora assegnato
            } else {
                votiTotali++;
                sommaVoti += voto.voto();

                if (voto.voto() < 6) insufficienze++;
                else sufficienze++;
            }
        }

        // Aggiorna i riepiloghi numerici
        votiInAttesaLabel.setText(String.valueOf(votiInAttesa));
        insufficienzeLabel.setText(String.valueOf(insufficienze));
        sufficienzeLabel.setText(String.valueOf(sufficienze));

        // Calcolo media con formattazione
        double media = (double) sommaVoti / votiTotali;
        mediaVoti.setText(String.format("%.2f", media));
    }

    // Aggiorna la lista dei voti visualizzati
    private void updateListVoti(List<ValutazioneStudente> voti) {
        // Ripulisce la lista e rigenera tutte le card dei voti
        listaVotiVBox.getChildren().clear();
        for (ValutazioneStudente voto : voti) {
            generaLabel(voto);
        }
    }

    // Genera una card per ogni voto
    private void generaLabel(ValutazioneStudente voto) {
        // Contenitore stile card
        BorderPane newBorderPane = new BorderPane();

        // Label materia + voto
        Label materia_Voto = new Label(
                voto.materia().toUpperCase() + ": " + voto.voto()
        );
        materia_Voto.setFont(Font.font("Helvetica", FontWeight.BOLD, FontPosture.REGULAR, 15));

        // Label data del voto
        Label date = new Label(voto.data());
        date.setFont(Font.font("Helvetica", FontWeight.NORMAL, FontPosture.REGULAR, 14));

        // Stile base card
        newBorderPane.setStyle(
                "-fx-border-radius: 10px;" +
                        "-fx-background-radius: 10px;" +
                        "-fx-padding: 10px;"
        );

        // Colori in base alla valutazione
        if (voto.voto() > 1 && voto.voto() < 6) {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #f55c47;");
        } else if (voto.voto() >= 6) {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #9fe6a0;");
        } else {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #e5e4e2;");
        }

        // Posizionamento elementi
        newBorderPane.setTop(materia_Voto);
        newBorderPane.setCenter(date);
        newBorderPane.setAlignment(materia_Voto, Pos.CENTER);
        newBorderPane.setAlignment(date, Pos.CENTER);

        listaVotiVBox.getChildren().add(newBorderPane);
    }

    // Aggiorna il grafico dell'andamento dei voti
    private void updateChart(List<ValutazioneStudente> voti) {
        // Crea una nuova serie per il grafico
        XYChart.Series<String, Number> series = new XYChart.Series<>();

        // Inserisce i dati nel grafico
        for (ValutazioneStudente voto : voti) {
            if (voto.voto() != 0) {
                XYChart.Data<String, Number> data = new XYChart.Data<>(voto.materia(), voto.voto());

                // Cambia colore della barra dopo la creazione del nodo grafico
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) {
                        if (voto.voto() >= 6) {
                            newNode.setStyle("-fx-background-color: #9fe6a0");
                        } else {
                            newNode.setStyle("-fx-background-color: #f55c47;");
                        }
                    }
                });

                series.getData().add(data);
            }
        }

        andamentoChart.getData().add(series);
        andamentoChart.setLegendVisible(false);  // Nasconde la legenda
    }

    // Metodo chiamato quando i dati dello studente vengono aggiornati (Observer Pattern)
    @Override
    public void update(Object event) {
        // Aggiorna solo se la notifica riguarda lo studente loggato
        if (event instanceof String && event.equals(studente)) {

            // Ricarica i voti aggiornati
            voti = Database.getInstance().getVotiStudente(studente);

            // Aggiorna grafico, lista voti e riepilogo
            updateChart(voti);
            updateListVoti(voti);
            updateRiepilogo(voti);
        }
    }
}
