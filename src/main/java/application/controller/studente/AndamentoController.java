package application.controller.studente;

import application.Database;
import application.ExportContext;
import application.PDFExportStrategy;
import application.SceneHandler;
import application.model.ValutazioneStudente;
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

public class AndamentoController {


    private List<String> materie = new ArrayList<>();
    private String studente;
    private List<ValutazioneStudente> voti = new ArrayList<>();

    @FXML
    private VBox listaVotiVBox;

    @FXML
    private BarChart<String, Number> andamentoChart;

    @FXML
    private CategoryAxis materieX;

    @FXML
    private Label votiInAttesaLabel;

    @FXML
    private Label insufficienzeLabel;

    @FXML
    private Label sufficienzeLabel;

    @FXML
    private Label mediaVoti;

    @FXML
    private Label nominativoStudente;

    @FXML
    private Label classeStudente;


    @FXML
    private void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setStudentHomePage(studente);
    }

    @FXML
    private void exportPDF(MouseEvent event) {

        //PDFGenerator.getInstance().pdfValutazione(voti);

        // Implementazione del pattern Strategy
        ExportContext exportContext = ExportContext.getInstance();
        exportContext.setStrategy(new PDFExportStrategy()); // 1. Imposta la strategia concreta (PDF)
        exportContext.exportValutazione(voti); // 2. Esegue il contesto
    }

    @FXML
    public void initialize() {
        studente = SceneHandler.getInstance().getUsername();
        materie = Database.getInstance().getAllMaterieIstituto();
        materieX.setCategories(FXCollections.observableArrayList(materie));
        voti = Database.getInstance().getVotiStudente(studente);

        nominativoStudente.setText(Database.getInstance().getFullName(studente).toUpperCase());
        classeStudente.setText(Database.getInstance().getClasseUser(studente).toUpperCase());

        updateChart(voti);
        updateListVoti(voti);
        updateRiepilogo(voti);
    }

    private void updateRiepilogo(List<ValutazioneStudente> voti) {
        int insufficienze = 0;
        int sufficienze = 0;
        int votiInAttesa = 0;
        int sommaVoti = 0;
        int votiTotali = 0;

        for (ValutazioneStudente voto : voti) {
            if (voto.voto() == 0) {
                votiInAttesa++;
            } else {
                votiTotali++;
                sommaVoti += voto.voto();
                if (voto.voto() < 6) {
                    insufficienze++;
                } else {
                    sufficienze++;
                }
            }
        }

        votiInAttesaLabel.setText(String.valueOf(votiInAttesa));
        insufficienzeLabel.setText(String.valueOf(insufficienze));
        sufficienzeLabel.setText(String.valueOf(sufficienze));
        double media = (double) sommaVoti / votiTotali;
        mediaVoti.setText(String.format("%.2f", media)); // Formatta la media a due decimali
    }

    private void updateListVoti(List<ValutazioneStudente> voti) {
        listaVotiVBox.getChildren().clear();
        for (ValutazioneStudente voto : voti) {
            generaLabel(voto);
        }
    }

    private void generaLabel(ValutazioneStudente voto) {
        BorderPane newBorderPane = new BorderPane();
        Label materia_Voto = new Label();
        materia_Voto.setText(voto.materia().toUpperCase() + ": " + voto.voto());
        materia_Voto.setFont(Font.font("Helvetica", FontWeight.BOLD, FontPosture.REGULAR, 15));

        Label date = new Label();
        date.setText(voto.data());
        date.setFont(Font.font("Helvetica", FontWeight.NORMAL, FontPosture.REGULAR, 14));

        newBorderPane.setStyle(
                "-fx-border-radius: 10px; " +
                        "-fx-background-radius: 10px; " +
                        "-fx-padding: 10px;");
        if (voto.voto() > 1 && voto.voto() < 6) {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #f55c47;");
        } else if (voto.voto() >= 6) {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #9fe6a0;");
        } else {
            newBorderPane.setStyle(newBorderPane.getStyle() + "-fx-background-color: #e5e4e2;");
        }


        newBorderPane.setTop(materia_Voto);
        newBorderPane.setCenter(date);
        newBorderPane.setAlignment(newBorderPane.getTop(), Pos.CENTER);
        newBorderPane.setAlignment(newBorderPane.getCenter(), Pos.CENTER);

        listaVotiVBox.getChildren().add(newBorderPane);
    }

    private void updateChart(List<ValutazioneStudente> voti) {

        XYChart.Series<String, Number> series = new XYChart.Series<>();


        for (ValutazioneStudente voto : voti) {
            if (voto.voto() != 0) {
                // Crea un oggetto XYChart.Data per ogni voto
                XYChart.Data<String, Number> data = new XYChart.Data<>(voto.materia(), voto.voto());

                // Aggiungi un ChangeListener per il nodo della barra
                data.nodeProperty().addListener((observable, oldNode, newNode) -> {
                    if (newNode != null) {
                        // Cambia il colore in base al valore del voto
                        if (voto.voto() >= 6) {
                            newNode.setStyle("-fx-background-color: #9fe6a0");
                        } else {
                            newNode.setStyle("-fx-background-color: #f55c47;");
                        }
                    }
                });

                // Aggiungi i dati alla serie
                series.getData().add(data);
            }
        }
        // Aggiungi la serie al grafico
        andamentoChart.getData().add(series);
        // Disabilita la legenda
        andamentoChart.setLegendVisible(false);
    }
}
