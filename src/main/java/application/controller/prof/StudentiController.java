package application.controller.prof;

import application.exportStrategy.CSVClasseStrategy;
import application.exportStrategy.ExportContext;
import application.exportStrategy.PDFClasseStrategy;
import application.model.Nota;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import application.observer.DatabaseObserver;
import application.persistence.Database;
import application.persistence.DatabaseEvent;
import application.persistence.DatabaseEventType;
import application.utility.MessageDebug;
import application.view.SceneHandler;
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
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class StudentiController implements DatabaseObserver {

    private ObservableList<StudenteTable> studenti = FXCollections.observableArrayList();
    private List<StudenteTable> studentiList;
    private final ExportContext exportContext = ExportContext.getInstance();
    private StudenteTable studenteSelezionato;

    private Database db;
    private SceneHandler sh;
    private String usernameProf, classeProf, materiaProf;

    @FXML
    private Label nominativoStudenteVotoPane, classeLabel, totalStudentsLabel, insufficientLabel, sufficientLabel, nominativoStudenteLabel;

    @FXML
    private BorderPane mainPane, addNotaPane, addVotoPane;

    @FXML
    private TableView<StudenteTable> studentiTableView;

    @FXML
    private TableColumn<StudenteTable, String> nameColumn, surnameColumn, dataValutazioneColumn;

    @FXML
    private TableColumn<StudenteTable, Integer> voteColumn;

    @FXML
    private TextField notaField, votoField;


    // Inizializza il controller e carica studenti e statistiche
    public void initialize() {

        db = Database.getInstance();
        sh = SceneHandler.getInstance();
        usernameProf = sh.getUsername();
        classeProf = db.getClasseUser(usernameProf);
        materiaProf = db.getMateriaProf(usernameProf);


        db.attach(this); // Si registra come observer del database

        classeLabel.setText(classeProf);
        studentiList = db.getStudentiClasse(classeProf, materiaProf);

        aggiornaNumeroStudenti();
        aggiornaAndamentoClasse();
        setValueFactory();
        studentiTableView.setItems(studenti);
    }

    // Aggiorna il numero totale di studenti
    private void aggiornaNumeroStudenti() {
        totalStudentsLabel.setText(String.valueOf(studentiList.size()));
    }

    // Aggiorna il numero di insufficienti e sufficienti
    private void aggiornaAndamentoClasse() {
        int insufficienti = 0;
        int sufficienti = 0;
        for (StudenteTable studente : studentiList) {
            if (studente.voto() < 6) insufficienti++;
            else sufficienti++;
        }
        insufficientLabel.setText(String.valueOf(insufficienti));
        sufficientLabel.setText(String.valueOf(sufficienti));
    }


    // Configura le colonne della tabella
    private void setValueFactory() {
        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nome().toUpperCase()));
        surnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().cognome().toUpperCase()));
        dataValutazioneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().dataValutazione()));
        voteColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().voto()).asObject());
        setStudents(studentiList);
    }

    // Aggiorna la lista degli studenti nella tabella
    private void setStudents(List<StudenteTable> studentiList) {
        studenti.clear();
        studenti.addAll(studentiList);
        studentiTableView.refresh();
    }


    // Mostra il pannello per aggiungere una nota
    @FXML
    private void showNotePaneClicked() {
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();
        if (studenteSelezionato == null) {
            sh.showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {
            addNotaPane.setVisible(true);
            nominativoStudenteLabel.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }
    }

    // Aggiunge una nota allo studente selezionato
    @FXML
    private void addNotaClicked() {
        String nota = notaField.getText();
        if (nota.trim().isEmpty()) {
            sh.showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            notaField.clear();
        } else {
            Nota notaDB = new Nota(studenteSelezionato.username(), usernameProf,
                    nota.toUpperCase(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            if (db.insertNota(notaDB)) {
                sh.showInformation(MessageDebug.NOTE_ADDED);
                notaField.clear();
                backNoteClicked();
            } else {
                sh.showWarning(MessageDebug.ERROR_NOTE_ADD);
            }
        }
    }

    // Aggiorna il voto dello studente selezionato
    @FXML
    private void updateVoto() {
        try {
            Integer newVoto = Integer.parseInt(votoField.getText());
            if (newVoto < 0 || newVoto > 10) {
                sh.showWarning(MessageDebug.VOTO_NOT_VALID);
            } else {
                ValutazioneStudente valutazione = new ValutazioneStudente(
                        studenteSelezionato.username(),
                        usernameProf,
                        materiaProf,
                        LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                        newVoto);
                if (db.updateVoto(valutazione)) {
                    sh.showInformation(MessageDebug.VOTO_UPDATED);
                    votoField.clear();
                    backVoteClickedVotoPane();
                } else {
                    sh.showWarning(MessageDebug.ERROR_VOTO_UPDATE);
                }
            }
        } catch (NumberFormatException e) {
            sh.showWarning(MessageDebug.VOTO_NOT_VALID);
        }
    }

    // Chiude il pannello di aggiunta nota
    @FXML
    private void backNoteClicked() {
        addNotaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Chiude il pannello di aggiunta voto
    @FXML
    public void backVoteClickedVotoPane() {
        addVotoPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Torna alla home del professore e rimuove l'observer
    @FXML
    private void backButtonClicked() throws IOException {
        db.detach(this);
        sh.setProfessorHomePage(usernameProf);
    }


    // Mostra il pannello di aggiunta voto
    public void showVotoPageClicked(ActionEvent actionEvent) {
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();
        if (studenteSelezionato == null) {
            sh.showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {
            addVotoPane.setVisible(true);
            nominativoStudenteVotoPane.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }
    }

    // Aggiorna la UI in base agli eventi del database
    @Override
    public void update(DatabaseEvent event) {
        if (event.type() == DatabaseEventType.VOTO_AGGIORNATO || event.type() == DatabaseEventType.NOTA_INSERITA) {
            System.out.println("StudentiController: ricevuto evento " + event.type());
            javafx.application.Platform.runLater(() -> {
                studentiList = db.getStudentiClasse(classeProf, materiaProf);

                aggiornaNumeroStudenti();
                aggiornaAndamentoClasse();
                setStudents(studentiList);

                System.out.println("Lista studenti e statistiche aggiornate correttamente.");
            });
        }
    }

    // Esporta l'andamento della classe in PDF
    @FXML
    private void exportPDF() {
        exportContext.setStrategy(new PDFClasseStrategy());
        exportContext.exportAndamentoClasse(studentiList);
    }

    // Esporta l'andamento della classe in CSV
    @FXML
    public void exportCSV(MouseEvent mouseEvent) {
        exportContext.setStrategy(new CSVClasseStrategy());
        exportContext.exportAndamentoClasse(studentiList);
    }
}