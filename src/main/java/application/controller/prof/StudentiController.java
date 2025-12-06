package application.controller.prof;

import application.Database;
import application.MessageDebug;
import application.SceneHandler;
import application.export.ExportContext;
import application.export.PDFClassExportStrategy;
import application.model.Nota;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentiController {

    private ObservableList<StudenteTable> studenti = FXCollections.observableArrayList();

    private List<StudenteTable> studentiList;

    @FXML
    private Label nominativoStudenteVotoPane;

    @FXML
    private BorderPane mainPane;

    @FXML
    private Label totalStudentsLabel;

    @FXML
    private Label insufficientLabel;

    @FXML
    private Label sufficientLabel;

    @FXML
    private TableView<StudenteTable> studentiTableView;

    @FXML
    private TableColumn<StudenteTable, String> nameColumn;

    @FXML
    private TableColumn<StudenteTable, String> surnameColumn;

    @FXML
    private TableColumn<StudenteTable, String> dataValutazioneColumn;

    @FXML
    private TableColumn<StudenteTable, Integer> voteColumn;


    @FXML
    private Label classeLabel;

    @FXML
    private BorderPane addNotaPane;

    @FXML
    private BorderPane addVotoPane;

    @FXML
    private Label nominativoStudenteLabel;

    @FXML
    private TextField notaField;

    @FXML
    private TextField votoField;

    @FXML
    private void exportPDF() {

        //PDFGenerator.getInstance().pdfAndamentoClasse(studentiList);
        // Implementazione del pattern Strategy
        ExportContext exportContext = ExportContext.getInstance();
        exportContext.setStrategy(new PDFClassExportStrategy()); // 1. Imposta la strategia concreta (PDF Classe)
        exportContext.exportAndamentoClasse(studentiList); // 2. Esegue il contesto
    }

    private StudenteTable studenteSelezionato = null;

    @FXML
    private void showNotePaneClicked() {

        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();

        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {
            addNotaPane.setVisible(true);
            nominativoStudenteLabel.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }
    }

    @FXML
    private void addNotaClicked() {
        String nota = notaField.getText();
        if (nota.trim().isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            nota = "";
        } else {
            Nota notaDB = new Nota(studenteSelezionato.username(), SceneHandler.getInstance().getUsername(), nota.toUpperCase(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());
            if (Database.getInstance().insertNota(notaDB)) {
                SceneHandler.getInstance().showInformation(MessageDebug.NOTE_ADDED);
                notaField.clear();
                addNotaPane.setVisible(false);
                mainPane.setDisable(false);
                mainPane.setEffect(null);
            } else {
                SceneHandler.getInstance().showWarning(MessageDebug.ERROR_NOTE_ADD);
            }
        }
    }

    @FXML
    private void updateVoto() {

        try {
            Integer newVoto = Integer.parseInt(votoField.getText());

            if (newVoto < 0 || newVoto > 10) {
                SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
            } else {
                ValutazioneStudente valutazione = new ValutazioneStudente(studenteSelezionato.username(), SceneHandler.getInstance().getUsername(), Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(), newVoto);
                if (Database.getInstance().updateVoto(valutazione)) {
                    SceneHandler.getInstance().showInformation(MessageDebug.VOTO_UPDATED);
                    votoField.clear();
                    addVotoPane.setVisible(false);
                    mainPane.setDisable(false);
                    mainPane.setEffect(null);
                    //Aggiorna la lista degli studenti e la tabella
                    studentiList = Database.getInstance().getStudentiClasse(
                            classeLabel.getText(),
                            Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername())
                    );
                    aggiornaNumeroStudenti();
                    aggiotnaAndamentoClasse();
                    setStudents(studentiList); // Aggiorna la tabella

                } else {
                    SceneHandler.getInstance().showWarning(MessageDebug.ERROR_VOTO_UPDATE);
                }
            }
        } catch (NumberFormatException e) {
            SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
        }
    }

    @FXML
    private void backNoteClicked() {
        addNotaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    @FXML
    private void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }


    public void initialize() {

        // Impostiamo la classe
        classeLabel.setText(Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername()));
        studentiList = Database.getInstance().getStudentiClasse(classeLabel.getText(), Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()));
        aggiornaNumeroStudenti();
        aggiotnaAndamentoClasse();

        // Inseriamo gli studenti della classe nella tabella
        setValueFactory();
        studentiTableView.setItems(studenti);

    }

    private void aggiotnaAndamentoClasse() {
        int insufficienti = 0;
        int sufficienti = 0;
        for (StudenteTable studente : studentiList) {
            if (studente.voto() < 6) {
                insufficienti++;
            } else {
                sufficienti++;
            }
        }
        insufficientLabel.setText(String.valueOf(insufficienti));
        sufficientLabel.setText(String.valueOf(sufficienti));
    }

    private void aggiornaNumeroStudenti() {

        totalStudentsLabel.setText(String.valueOf(studentiList.size()));
    }

    private void setValueFactory() {

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nome().toUpperCase()));
        surnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().cognome().toUpperCase()));
        dataValutazioneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().dataValutazione()));
        voteColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().voto()).asObject());

        setStudents(studentiList);

    }

    private void setStudents(List<StudenteTable> studentiList) {
        studenti.clear();
        studenti.addAll(studentiList);
        studentiTableView.refresh();
    }

    public void showVotoPageClicked(ActionEvent actionEvent) {

        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();

        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {
            addVotoPane.setVisible(true);
            nominativoStudenteVotoPane.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }

    }

    public void backVoteClickedVotoPane() {
        addVotoPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }
}
