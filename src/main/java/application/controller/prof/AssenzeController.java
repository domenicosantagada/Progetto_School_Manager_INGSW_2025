package application.controller.prof;

import application.dao.AssenzeDAO;
import application.dao.VotiDAO;
import application.model.Assenza;
import application.model.StudenteTable;
import application.observer.DatabaseObserver;
import application.persistence.Database;
import application.persistence.DatabaseEvent;
import application.utility.MessageDebug;
import application.view.SceneHandler;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.TextStyle;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Controller per la gestione delle assenze del professore.
 * Mostra un calendario mensile con studenti e giorni.
 */
public class AssenzeController implements DatabaseObserver {

    /* =======================
       ====== FXML ===========
       ======================= */

    @FXML private ChoiceBox<String> studentChoiceBox;
    @FXML private DatePicker absenceDatePicker;
    @FXML private BorderPane mainPane;
    @FXML private BorderPane inputPane;
    @FXML private Label monthLabel;
    @FXML private Label classLabel;
    @FXML private GridPane calendarGrid;

    /* =======================
       ====== DATI ===========
       ======================= */

    private String prof;
    private String classe;
    private String materia;

    private List<StudenteTable> studenti;
    private final Map<StudenteTable, Integer> studentRowMap = new HashMap<>();

    private LocalDate meseCorrente;

    /* =======================
       ==== INITIALIZE =======
       ======================= */

    @FXML
    public void initialize() {
        Database.getInstance().attach(this);

        setupStylesheet();
        setupInitialState();
        loadProfessorInfo();
        loadStudents();
        setupStudentChoiceBox();

        refreshCalendar();
    }

    /* =======================
       ===== SETUP ===========
       ======================= */

    private void setupStylesheet() {
        classLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets()
                        .add(getClass().getResource("/css/style.css").toExternalForm());
            }
        });
    }

    private void setupInitialState() {
        inputPane.setVisible(false);
        meseCorrente = LocalDate.now();
        updateMonthLabel();
    }

    private void loadProfessorInfo() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);
        materia = Database.getInstance().getMateriaProf(prof);
        classLabel.setText(classe);
    }

    private void loadStudents() {
        studenti = Database.getInstance().getStudentiClasse(classe, materia);
        studenti.sort(Comparator.comparing(StudenteTable::cognome, String.CASE_INSENSITIVE_ORDER));
        mapStudentsToRows();
    }

    private void mapStudentsToRows() {
        studentRowMap.clear();
        for (int i = 0; i < studenti.size(); i++) {
            studentRowMap.put(studenti.get(i), i);
        }
    }

    private void setupStudentChoiceBox() {
        studentChoiceBox.getItems().clear();
        for (StudenteTable s : studenti) {
            studentChoiceBox.getItems().add(
                    s.cognome().toUpperCase() + " " + s.nome().toUpperCase()
            );
        }
    }

    /* =======================
       ===== CALENDARIO ======
       ======================= */

    private void refreshCalendar() {
        populateCalendarGrid();
        loadAndDrawAbsences();
    }

    private void populateCalendarGrid() {
        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        setupColumns();
        setupRows();
        addStudentLabels();
        addDayLabels();
    }

    private void setupColumns() {
        ColumnConstraints studentColumn = new ColumnConstraints(170);
        calendarGrid.getColumnConstraints().add(studentColumn);

        int daysInMonth = meseCorrente.lengthOfMonth();
        for (int i = 0; i < daysInMonth; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setMinWidth(20);
            dayColumn.setHgrow(Priority.ALWAYS);
            calendarGrid.getColumnConstraints().add(dayColumn);
        }
    }

    private void setupRows() {
        for (int i = 0; i <= studenti.size(); i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.SOMETIMES);
            calendarGrid.getRowConstraints().add(row);
        }
    }

    private void addStudentLabels() {
        for (int i = 0; i < studenti.size(); i++) {
            StudenteTable s = studenti.get(i);
            Label label = new Label(" " + s.cognome().toUpperCase() + " " + s.nome().toUpperCase());
            label.getStyleClass().add("cell");
            calendarGrid.add(label, 0, i + 1);
            GridPane.setHalignment(label, HPos.CENTER);
            GridPane.setValignment(label, VPos.CENTER);
        }
    }

    private void addDayLabels() {
        int daysInMonth = meseCorrente.lengthOfMonth();
        for (int i = 0; i < daysInMonth; i++) {
            Label label = new Label(String.valueOf(i + 1));
            label.getStyleClass().add("cell");
            calendarGrid.add(label, i + 1, 0);
            GridPane.setHalignment(label, HPos.CENTER);
            GridPane.setValignment(label, VPos.CENTER);
        }
    }

    private void loadAndDrawAbsences() {
        for (StudenteTable s : studenti) {
            int row = studentRowMap.get(s);

            // Prendi tutte le assenze dello studente e ordinale per data
            List<Assenza> assenze = new ArrayList<>(Database.getInstance()
                    .getAssenzeStudente(s.username(), meseCorrente.getMonthValue()));
            assenze.sort(Comparator.comparingInt(Assenza::anno)
                    .thenComparingInt(Assenza::mese)
                    .thenComparingInt(Assenza::giorno));

            for (Assenza a : assenze) {
                drawAbsenceCell(row, a, a.giustificata(), "red");
            }
        }
    }

    private void drawAbsenceCell(int row, Assenza a, boolean giustificata, String tipoDiRosso) {
        StackPane cell = new StackPane();
        Label label = new Label(giustificata ? "G" : "A");

        label.setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
        cell.setStyle(giustificata
                ? "-fx-background-color: blue;"
                : "-fx-background-color: " + tipoDiRosso + ";");

        cell.getChildren().add(label);
        if(!giustificata){
            AtomicInteger count = new AtomicInteger(0);
            cell.setOnMouseClicked(e -> {
                count.getAndIncrement();
                if(count.get() == 2) {
                    AssenzeDAO assenzeDao = new AssenzeDAO();
                    assenzeDao.justifyAssenza(a, "");
                    cell.setStyle("-fx-background-color: blue");
                    label.setText("G");
                    count.set(0);
                }
            });
        }

        ContextMenu menu = new ContextMenu();
        MenuItem delete = new MenuItem("Elimina assenza");

        delete.setOnAction(e -> {
            StudenteTable studente = studenti.get(row);
            Database.getInstance().deleteAssenza(
                    studente.username(),
                    a.giorno(),
                    meseCorrente.getMonthValue(),
                    meseCorrente.getYear()
            );
            calendarGrid.getChildren().remove(cell);
        });

        menu.getItems().add(delete);
        cell.setOnContextMenuRequested(e ->
                menu.show(cell, e.getScreenX(), e.getScreenY()));

        calendarGrid.add(cell, a.giorno(), row + 1);
    }

    private boolean quintaAssenza(List<Assenza> assenze, Assenza a) {
        LocalDate previousDate = null;
        int consecutiveCount = 0;

        for (Assenza ass : assenze) {
            LocalDate assDate = LocalDate.of(ass.anno(), ass.mese(), ass.giorno());

            if (!ass.giustificata()) {
                if (previousDate != null && assDate.equals(previousDate.plusDays(1))) {
                    consecutiveCount++;
                } else {
                    consecutiveCount = 1; // reset se non consecutiva
                }

                if (ass.equals(a) && consecutiveCount >= 5) {
                    return true; // la nostra assenza corrente è la quinta consecutiva o più
                }
            } else {
                consecutiveCount = 0; // reset se giustificata
            }

            previousDate = assDate;
        }

        return false;
    }

    /* =======================
       ====== AZIONI =========
       ======================= */

    @FXML
    private void aggiungiAssenzaStudente() {
        if (studentChoiceBox.getSelectionModel().isEmpty() || absenceDatePicker.getValue() == null) {
            SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            return;
        }

        int index = studentChoiceBox.getSelectionModel().getSelectedIndex();
        StudenteTable studente = studenti.get(index);
        LocalDate data = absenceDatePicker.getValue();

        Assenza assenza = new Assenza(
                studente.username(),
                data.getDayOfMonth(),
                data.getMonthValue(),
                data.getYear(),
                "Assenza non giustificata",
                false
        );

        Database.getInstance().addAssenza(assenza);
        onCancelAddAbsence();
    }

    @FXML
    public void showAddAssenzaPane() {
        studentChoiceBox.getSelectionModel().clearSelection();
        absenceDatePicker.setValue(LocalDate.now());
        mainPane.setDisable(true);
        mainPane.setEffect(new GaussianBlur());
        inputPane.setVisible(true);
    }

    @FXML
    private void onCancelAddAbsence() {
        inputPane.setVisible(false);
        mainPane.setDisable(false);
        mainPane.setEffect(null);
    }

    @FXML
    private void backButtonClicked() throws IOException {
        Database.getInstance().detach(this);
        SceneHandler.getInstance().setProfessorHomePage(prof);
    }

    public void mesePrecedente() {
        meseCorrente = meseCorrente.minusMonths(1);
        updateMonthLabel();
        refreshCalendar();
    }

    public void meseSuccessivo() {
        meseCorrente = meseCorrente.plusMonths(1);
        updateMonthLabel();
        refreshCalendar();
    }

    private void updateMonthLabel() {
        monthLabel.setText(
                meseCorrente.getMonth()
                        .getDisplayName(TextStyle.FULL, Locale.ITALIAN)
        );
    }

    /* =======================
       ===== OBSERVER ========
       ======================= */

    @Override
    public void update(DatabaseEvent event) {
        switch (event.type()) {
            case ASSENZA_AGGIUNTA:
            case ASSENZA_GIUSTIFICATA:
            case ASSENZA_ELIMINATA:
                Platform.runLater(this::refreshCalendar);
                break;
            default:
                break;
        }
    }
}