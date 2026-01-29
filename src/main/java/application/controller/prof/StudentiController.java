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
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class StudentiController implements DatabaseObserver {
    /* =========================
               FXML COMPONENTS
               ========================= */
    @FXML private Label nominativoStudenteVotoPane, classeLabel, totalStudentsLabel, insufficientLabel, sufficientLabel, nominativoStudenteLabel;
    @FXML private BorderPane mainPane, addNotaPane, addVotoPane;
    @FXML private TableView<StudenteTable> studentiTableView;
    @FXML private TableColumn<StudenteTable, String> nameColumn, surnameColumn, dataValutazioneColumn;
    //@FXML private TableColumn<StudenteTable, Void> eliminaColumn;
    @FXML private TableColumn<StudenteTable, Integer> voteColumn;
    @FXML private TextField notaField, votoField;
    @FXML private Button showVotoPane;
    @FXML private ComboBox<StudenteTable> studentiBox;
    @FXML public VBox studenteScelto;

    /* =========================
       VARIABLES
       ========================= */
    private ObservableList<StudenteTable> studenti = FXCollections.observableArrayList();
    private List<StudenteTable> studentiList;
    private final ExportContext exportContext = ExportContext.getInstance();
    private StudenteTable studenteSelezionato;

    private static final String DEFAULT_ROW_STYLE = "-fx-background-color: transparent;";
    private static final String SUFFICIENT_ROW_STYLE = "-fx-background-color: #9fe6a0;";
    private static final String INSUFFICIENT_ROW_STYLE = "-fx-background-color: #f55c47;";
    private static final String FAIL_ROW_STYLE = "-fx-background-color: #e53935;";

    /* =========================
       INITIALIZE
       ========================= */
    public void initialize() {
        Database.getInstance().attach(this);

        classeLabel.setText(Database.getInstance().getClasseUser(SceneHandler.getInstance().getUsername()));
        studentiList = Database.getInstance().getStudentiClasse(classeLabel.getText(),
                Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()));

        setupTable();
        setupRowStyle();
        aggiornaStatistiche();
        bindButtonsToSelection();
        setStudents(studentiList);

        showVotoPane.setOnAction(actionEvent -> {
            if(studenteSelezionato != null) {
                String input = showAlert("Nuovo voto", "Inserisci il nuovo voto:", true);
                if(input != null) {
                    int voto = Integer.parseInt(input);
                    updateVoto(voto);
                }
            }
        });

        setupCombobox();
    }

    /* =========================
       TABLE SETUP
       ========================= */
    private void setupTable() {
        /* eliminaColumn.setCellFactory(col -> new TableCell<>() {

            private final Button btn = new Button("Elimina");

            {
                btn.setOnAction(e -> {
                    var item = getTableView().getItems().get(getIndex());
                    getTableView().getItems().remove(item);
                    Database.getInstance().removeStudente(item.username());
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });*/
        nameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().nome().toUpperCase()));
        surnameColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().cognome().toUpperCase()));
        dataValutazioneColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().dataValutazione()));
        voteColumn.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().voto()).asObject());
    }

    /* =========================
       ROW STYLE MANAGEMENT
       ========================= */
    private void setupRowStyle() {
        studentiTableView.setRowFactory(tv -> {
            TableRow<StudenteTable> row = new TableRow<>() {
                @Override
                protected void updateItem(StudenteTable studente, boolean empty) {
                    super.updateItem(studente, empty);
                    //applyRowStyle(this, studente, empty, DEFAULT_ROW_STYLE);
                }
            };

            AtomicInteger counter = new AtomicInteger(0);
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty()) {
                    StudenteTable selezionato = row.getItem();
                    studenteSelezionato = selezionato; // aggiorniamo lo studente selezionato

                    counter.getAndIncrement();
                    if(studenteSelezionato != null && counter.get() == 2) {
                        String input = showAlert("Nuovo voto", "Inserisci il nuovo voto:", true);
                        if (input != null) {
                            try {
                                int voto = Integer.parseInt(input);
                                updateVoto(voto);
                                counter.set(0);
                            } catch (NumberFormatException e) {
                                SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
                            }
                        }
                    }
                }
            });

            return row;
        });
    }

    private void setupCombobox(){
        studentiBox.setItems(
                FXCollections.observableArrayList(studenti)
        );
        studentiBox.getSelectionModel()
                .selectedItemProperty()
                .addListener((obs, oldValue, newValue) -> {
                    System.out.println(studentiBox.getSelectionModel().getSelectedItem().nome());
                    studenteScelto.getChildren().clear();
                    studenteScelto.getChildren().addAll(new Label(newValue.nome()), new Label(newValue.cognome()), new Label(newValue.dataValutazione()), new Label(String.valueOf(newValue.voto())));
                });

        studentiBox.setConverter(new StringConverter<>() {
            @Override
            public String toString(StudenteTable s) {
                if (s == null) return "";
                return s.cognome() + " " + s.nome();
            }

            @Override
            public StudenteTable fromString(String string) {
                return null;
            }
        });
    }


    private void applyRowStyle(TableRow<StudenteTable> row, StudenteTable studente, boolean empty, String style) {
        if (empty || studente == null) {
            row.setStyle(style);
            return;
        }

        //if (studente.voto() < 5) row.setStyle(FAIL_ROW_STYLE);
        //else if (studente.voto() < 6) row.setStyle(INSUFFICIENT_ROW_STYLE);
        //else row.setStyle(SUFFICIENT_ROW_STYLE);
    }

    public void resetRowStyles() {
        studentiTableView.getItems().forEach(s -> {
            TableRow<StudenteTable> row = (TableRow<StudenteTable>) studentiTableView.lookup(".table-row-cell");
            if (row != null) row.setStyle(DEFAULT_ROW_STYLE);
        });
    }

    /* =========================
       STATISTICS
       ========================= */
    private void aggiornaStatistiche() {
        int insufficienti = 0, sufficienti = 0;
        for (StudenteTable s : studentiList) {
            if (s.voto() < 6) insufficienti++;
            else sufficienti++;
        }
        insufficientLabel.setText(String.valueOf(insufficienti));
        sufficientLabel.setText(String.valueOf(sufficienti));
        totalStudentsLabel.setText(String.valueOf(studentiList.size()));
    }

    /* =========================
       SELECTION BINDING
       ========================= */
    private void bindButtonsToSelection() {
        BooleanBinding nothingSelected = studentiTableView.getSelectionModel().selectedItemProperty().isNull();
        // esempio di binding: disabilita pulsanti se niente selezionato
        // puoi collegare qui altri bottoni se servono
    }

    /* =========================
       STUDENT MANAGEMENT
       ========================= */
    private void setStudents(List<StudenteTable> studentiList) {
        studenti.clear();
        studenti.addAll(studentiList);
        studentiTableView.setItems(studenti);
        studentiTableView.refresh();
    }

    public String showAlert(String title, String message, boolean withPrompt) {
        if (withPrompt) {
            TextInputDialog dialog = new TextInputDialog();
            dialog.setTitle(title);
            dialog.setHeaderText(null);
            dialog.setContentText(message);

            return dialog.showAndWait().orElse(null);
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle(title);
            alert.setHeaderText(null);
            alert.setContentText(message);
            alert.showAndWait();
            return null;
        }
    }

    @FXML
    private void showNotePaneClicked() {
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();
        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
            return;
        }
        addNotaPane.setVisible(true);
        mainPane.setDisable(true);
        mainPane.setEffect(new GaussianBlur());
        nominativoStudenteLabel.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
    }

    @FXML
    private void addNotaClicked() {
        String nota = notaField.getText();
        if (nota.trim().isEmpty()) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            notaField.clear();
            return;
        }

        Nota notaDB = new Nota(studenteSelezionato.username(), SceneHandler.getInstance().getUsername(),
                nota.toUpperCase(), LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));

        if (Database.getInstance().insertNota(notaDB)) {
            SceneHandler.getInstance().showInformation(MessageDebug.NOTE_ADDED);
            notaField.clear();
            backNoteClicked();
        } else {
            SceneHandler.getInstance().showWarning(MessageDebug.ERROR_NOTE_ADD);
        }
    }

    @FXML
    private void showVotoPageClicked(ActionEvent actionEvent) {
        studenteSelezionato = studentiTableView.getSelectionModel().getSelectedItem();
        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
            return;
        }
        addVotoPane.setVisible(true);
        mainPane.setDisable(true);
        mainPane.setEffect(new GaussianBlur());
        nominativoStudenteVotoPane.setText(studenteSelezionato.cognome().toUpperCase() + " " + studenteSelezionato.nome().toUpperCase());
    }

    public void updateVoto(int newVoto) {
        if (studenteSelezionato == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.STUDENT_NOT_SELECTED);
            return;
        }

        if (newVoto < 0 || newVoto > 10) {
            SceneHandler.getInstance().showWarning(MessageDebug.VOTO_NOT_VALID);
            return;
        }

        ValutazioneStudente valutazione = new ValutazioneStudente(
                studenteSelezionato.username(),
                SceneHandler.getInstance().getUsername(),
                Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername()),
                LocalDate.now().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                newVoto
        );

        if (Database.getInstance().updateVoto(valutazione)) {
            SceneHandler.getInstance().showInformation(MessageDebug.VOTO_UPDATED);
            backVoteClickedVotoPane();
        } else {
            SceneHandler.getInstance().showWarning(MessageDebug.ERROR_VOTO_UPDATE);
        }
    }

    /* =========================
       BACK ACTIONS
       ========================= */
    @FXML private void backNoteClicked() {
        addNotaPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    @FXML private void backVoteClickedVotoPane() {
        addVotoPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    @FXML private void backButtonClicked() throws IOException {
        Database.getInstance().detach(this);
        SceneHandler.getInstance().setProfessorHomePage(SceneHandler.getInstance().getUsername());
    }

    /* =========================
       EXPORT
       ========================= */
    @FXML
    private void exportPDF() {
        exportContext.setStrategy(new PDFClasseStrategy());
        exportContext.exportAndamentoClasse(studentiList);
    }

    @FXML
    private void exportCSV(MouseEvent mouseEvent) {
        exportContext.setStrategy(new CSVClasseStrategy());
        exportContext.exportAndamentoClasse(studentiList);
    }

    /* =========================
       DATABASE OBSERVER
       ========================= */
    @Override
    public void update(DatabaseEvent event) {
        if (event.type() == DatabaseEventType.VOTO_AGGIORNATO || event.type() == DatabaseEventType.NOTA_INSERITA) {
            Platform.runLater(() -> {
                studentiList = Database.getInstance().getStudentiClasse(
                        classeLabel.getText(),
                        Database.getInstance().getMateriaProf(SceneHandler.getInstance().getUsername())
                );
                aggiornaStatistiche();
                setStudents(studentiList);
            });
        }
    }
}