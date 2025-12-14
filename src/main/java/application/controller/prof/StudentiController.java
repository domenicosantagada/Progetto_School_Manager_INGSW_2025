package application.controller.prof;

import application.Database;
import application.MessageDebug;
import application.SceneHandler;
import application.export.ExportContext;
import application.export.PDFClassExportStrategy;
import application.model.Nota;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import application.observer.DataObserver;
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

public class StudentiController implements DataObserver {

    // Lista osservabile per la tabella degli studenti
    private ObservableList<StudenteTable> studenti = FXCollections.observableArrayList();

    // Lista normale per la gestione interna degli studenti
    private List<StudenteTable> studentiList;

    // Studente selezionato nella tabella
    private StudenteTable studenteSelezionato;

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

    // Metodo per esportare l'andamento della classe in PDF
    // Utilizza il pattern Strategy per scegliere la strategia di esportazione
    @FXML
    private void exportPDF() {
        // Implementazione del pattern Strategy
        // Creiamo il contesto di esportazione
        ExportContext exportContext = ExportContext.getInstance();

        // Impostiamo la strategia concreta desiderata (PDF o CSV)
        exportContext.setStrategy(new PDFClassExportStrategy());
        //exportContext.setStrategy(new CSVClassExportStrategy());

        // Eseguiamo l'esportazione
        exportContext.exportAndamentoClasse(studentiList);
    }

    // Metodo per mostrare il pannello di aggiunta nota
    @FXML
    private void showNotePaneClicked() {

        // Otteniamo lo studente selezionato dalla tabella
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();

        // Controlliamo se uno studente è stato effettivamente selezionato
        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {
            // Mostriamo il pannello per aggiungere una nota
            addNotaPane.setVisible(true);
            // Impostiamo il nome dello studente selezionato nel pannello
            nominativoStudenteLabel.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            // Disabilitiamo il pannello principale e applichiamo un effetto di sfocatura
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }
    }

    // Metodo per aggiungere una nota allo studente selezionato
    @FXML
    private void addNotaClicked() {

        // Otteniamo il testo della nota dal campo di input e verifichiamo che non sia vuoto
        String nota = notaField.getText();
        if (nota.trim().isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            nota = "";
        } else {

            // Creiamo un oggetto Nota con i dati necessari
            Nota notaDB = new Nota(studenteSelezionato.username(), SceneHandler.getInstance().getUsername(), nota.toUpperCase(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString());

            // Tentiamo di inserire la nota nel database
            if (Database.getInstance().insertNota(notaDB)) {
                // In caso di successo, mostriamo un messaggio di conferma e resettiamo il pannello
                SceneHandler.getInstance().showInformation(MessageDebug.NOTE_ADDED);
                notaField.clear();
                backNoteClicked();
            } else {
                // In caso di errore, mostriamo un messaggio di avviso
                SceneHandler.getInstance().showWarning(MessageDebug.ERROR_NOTE_ADD);
            }
        }
    }

    // Metodo per aggiornare il voto dello studente selezionato
    @FXML
    private void updateVoto() {

        try {
            // Otteniamo il nuovo voto dal campo di input
            Integer newVoto = Integer.parseInt(votoField.getText());

            // Verifichiamo che il voto sia valido (tra 0 e 10)
            if (newVoto < 0 || newVoto > 10) {
                SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
            } else {

                // Creiamo un oggetto ValutazioneStudente con i dati necessari
                ValutazioneStudente valutazione = new ValutazioneStudente(studenteSelezionato.username(), SceneHandler.getInstance().getUsername(), Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")).toString(), newVoto);

                // Tentiamo di aggiornare il voto nel database
                if (Database.getInstance().updateVoto(valutazione)) {
                    // In caso di successo, mostriamo un messaggio di conferma e resettiamo il pannello
                    SceneHandler.getInstance().showInformation(MessageDebug.VOTO_UPDATED);
                    votoField.clear();
                    backVoteClickedVotoPane();
                } else {
                    // In caso di errore, mostriamo un messaggio di avviso
                    SceneHandler.getInstance().showWarning(MessageDebug.ERROR_VOTO_UPDATE);
                }
            }
        } catch (NumberFormatException e) {
            SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
        }
    }

    // Metodo per chiudere il pannello di aggiunta nota
    @FXML
    private void backNoteClicked() {
        addNotaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Metodo per chiudere il pannello di aggiunta voto
    @FXML
    public void backVoteClickedVotoPane() {
        addVotoPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    // Metodo per tornare alla home page del professore
    @FXML
    private void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }


    public void initialize() {

        // Registriamo questo controller come osservatore del database
        Database.getInstance().attach(this);

        // Impostiamo la classe del professore e carichiamo gli studenti associati
        classeLabel.setText(Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername()));
        studentiList = Database.getInstance().getStudentiClasse(classeLabel.getText(), Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()));

        aggiornaNumeroStudenti();
        aggiornaAndamentoClasse();

        // Configuriamo le colonne della tabella e carichiamo i dati
        setValueFactory();
        studentiTableView.setItems(studenti);

    }

    // Metodo per aggiornare l'andamento della classe (numero di sufficienti e insufficienti)
    private void aggiornaAndamentoClasse() {
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

    // Metodo per aggiornare il numero totale di studenti
    private void aggiornaNumeroStudenti() {
        totalStudentsLabel.setText(String.valueOf(studentiList.size()));
    }

    // Metodo per configurare le colonne della tabella degli studenti
    private void setValueFactory() {

        nameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().nome().toUpperCase()));
        surnameColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().cognome().toUpperCase()));
        dataValutazioneColumn.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().dataValutazione()));
        voteColumn.setCellValueFactory(cellData -> new SimpleIntegerProperty(cellData.getValue().voto()).asObject());

        setStudents(studentiList);

    }

    // Metodo per aggiornare la lista degli studenti nella tabella
    private void setStudents(List<StudenteTable> studentiList) {
        studenti.clear();
        studenti.addAll(studentiList);
        studentiTableView.refresh();
    }

    // Metodo per mostrare il pannello di aggiunta voto
    public void showVotoPageClicked(ActionEvent actionEvent) {

        // Otteniamo lo studente selezionato dalla tabella
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();

        // Controlliamo se uno studente è stato effettivamente selezionato
        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
        } else {

            // Mostriamo il pannello per aggiungere un voto
            addVotoPane.setVisible(true);
            nominativoStudenteVotoPane.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
            // Disabilitiamo il pannello principale e applichiamo un effetto di sfocatura
            mainPane.setDisable(true);
            mainPane.setEffect(new GaussianBlur());
        }

    }

    // Metodo chiamato quando i dati osservati cambiano (Pattern Observer)
    @Override
    public void update(Object event) {

        // Ricarica i dati dal Database
        studentiList = Database.getInstance().getStudentiClasse(
                classeLabel.getText(),
                Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername())
        );

        // Aggiorna le statistiche e la tabella
        aggiornaNumeroStudenti();
        aggiornaAndamentoClasse();
        setStudents(studentiList);
    }
}