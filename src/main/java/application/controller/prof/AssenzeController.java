package application.controller.prof;

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
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/*
Controller per la gestione delle assenze degli studenti da parte del professore.
Permette di visualizzare un calendario con le assenze degli studenti e di aggiungerne di nuove.
 */
public class AssenzeController implements DatabaseObserver {

    /* =======================
       ====== FXML ===========
       ======================= */
    @FXML
    private ChoiceBox<String> studentChoiceBox;

    @FXML
    private DatePicker absenceDatePicker;

    @FXML
    private BorderPane mainPane;

    @FXML
    private BorderPane inputPane;

    @FXML
    private Label monthLabel;

    @FXML
    private Label classLabel;

    @FXML
    private GridPane calendarGrid;

    /* =======================
       ====== VARIABILI ======
       ======================= */
    private String prof;
    private String classe;
    private String materia;

    private List<StudenteTable> studenti;
    private final Map<StudenteTable, Integer> studentToRowMap = new HashMap<>();

    private LocalDate mese;

    // Inizializza il controller
    @FXML
    public void initialize() {
        Database.getInstance().attach(this);    // Aggiunge l'observer al database

        setupStyleSheet();  // Imposta lo stile CSS
        setupInitialState(); // Imposta lo stato iniziale della UI
        setProfClasse(); // Imposta le info di classe e professore
        loadStudents(); //  Carica gli studenti dalla classe
        setupStudentChoiceBox(); // Imposta la ChoiceBox degli studenti

        refreshCalendar(); // Carica il calendario
    }

    private void setupStyleSheet() {
        classLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }
        });
    }

    private void setupInitialState() {
        inputPane.setVisible(false);
        mese = LocalDate.now();
        updateMonthLabel();
    }

    private void loadStudents() {
        studenti = Database.getInstance().getStudentiClasse(classe, materia);
        studenti.sort((s1, s2) -> s1.cognome().compareToIgnoreCase(s2.cognome()));
        mapStudentsToGridRows();
    }

    // Mappa ogni studente alla riga della griglia
    private void mapStudentsToGridRows() {
        studentToRowMap.clear(); // Inizializza il mappa;
        for (StudenteTable s : studenti) {
            studentToRowMap.put(s, studenti.indexOf(s));
        }
    }

    private void setupStudentChoiceBox() {
        studentChoiceBox.getItems().clear(); // Inizializza la ChoiceBox
        for (StudenteTable s : studenti) {
            studentChoiceBox.getItems().add(s.cognome().toUpperCase() + " " + s.nome().toUpperCase());
        }
    }


    private void refreshCalendar() {
        populateCalendar();
        loadAndRenderAbsences();
    }

    // Carica le assenze dal database e le mostra nel calendario
    private void loadAndRenderAbsences() {
        for (StudenteTable s : studenti) {
            List<Assenza> assenzeStudente = Database.getInstance().getAssenzeStudente(s.username(), mese.getMonthValue());
            for (Assenza a : assenzeStudente) {
                drawAbsenceIndicator(studentToRowMap.get(s), a.giorno(), a.giustificata());
            }
        }
    }

    // Imposta il mese corrente nella label
    private void updateMonthLabel() {
        monthLabel.setText(mese.getMonth().getDisplayName(TextStyle.FULL, Locale.ITALIAN));
    }

    // Imposta professore, classe e materia
    private void setProfClasse() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);
        materia = Database.getInstance().getMateriaProf(prof);
        classLabel.setText(classe);
    }

    // Aggiunge una nuova assenza selezionata
    @FXML
    private void aggiungiAssenzaStudente() {
        try {
            if (studentChoiceBox.getSelectionModel().isEmpty() || absenceDatePicker.getValue() == null) {
                SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            }

            Integer posizione = studentToRowMap.get(studenti.get(studentChoiceBox.getSelectionModel().getSelectedIndex()));
            Integer giornoAssenza = absenceDatePicker.getValue().getDayOfMonth();
            StudenteTable studente = studenti.get(posizione);

            Assenza assenza = new Assenza(studente.username(), giornoAssenza, absenceDatePicker.getValue().getMonthValue(),
                    absenceDatePicker.getValue().getYear(), "Assenza non giustificata", false);

            Database.getInstance().addAssenza(assenza);
            drawAbsenceIndicator(posizione, giornoAssenza, false);
            onCancelAddAbsence();

        } catch (Exception e) {
            System.out.println("Errore nell'aggiunta dell'assenza");
        }
    }

    // Disegna l'indicatore di assenza nella griglia
    private void drawAbsenceIndicator(Integer posizione, Integer giorno, boolean giustificata) {
        StackPane cellaColorata = new StackPane();
        Label testoLabel = new Label();
        testoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        if (giustificata) {
            cellaColorata.setStyle("-fx-background-color: blue;");
            testoLabel.setText("G");
        } else {
            cellaColorata.setStyle("-fx-background-color: red;");
            testoLabel.setText("A");
        }

        cellaColorata.getChildren().add(testoLabel);

        ContextMenu contextMenu = new ContextMenu();
        MenuItem deleteItem = new MenuItem("Elimina Assenza");
        deleteItem.setOnAction(e -> {
            StudenteTable studente = studenti.get(posizione);
            Database.getInstance().deleteAssenza(studente.username(), giorno, mese.getMonthValue(), mese.getYear());
            SceneHandler.getInstance().showInformation("Assenza eliminata correttamente.");
            calendarGrid.getChildren().remove(cellaColorata);
        });
        contextMenu.getItems().add(deleteItem);
        cellaColorata.setOnContextMenuRequested(e -> contextMenu.show(cellaColorata, e.getScreenX(), e.getScreenY()));

        calendarGrid.add(cellaColorata, giorno, posizione + 1);
    }

    // Torna alla pagina precedente
    @FXML
    private void backButtonClicked() throws IOException {
        Database.getInstance().detach(this);
        SceneHandler.getInstance().setProfessorHomePage(prof);
    }

    // Nasconde il pannello di aggiunta assenza
    @FXML
    private void onCancelAddAbsence() {
        inputPane.setVisible(false);
        mainPane.setVisible(true);
        mainPane.setEffect(null);
        mainPane.setDisable(false);
    }

    // Popola la griglia del calendario con giorni e studenti
    private void populateCalendar() {
        Scene scene = calendarGrid.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }

        Integer dayOfMonth = mese.lengthOfMonth();
        //studenti.sort((s1, s2) -> s1.cognome().compareToIgnoreCase(s2.cognome()));

        calendarGrid.getChildren().clear();
        calendarGrid.getColumnConstraints().clear();
        calendarGrid.getRowConstraints().clear();

        ColumnConstraints studentColumn = new ColumnConstraints();
        studentColumn.setHgrow(Priority.NEVER);
        studentColumn.setPrefWidth(170);
        calendarGrid.getColumnConstraints().add(studentColumn);

        for (int i = 0; i < dayOfMonth; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setHgrow(Priority.ALWAYS);
            dayColumn.setMinWidth(20);
            calendarGrid.getColumnConstraints().add(dayColumn);
        }

        for (int i = 0; i < studenti.size() + 1; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.SOMETIMES);
            calendarGrid.getRowConstraints().add(row);
        }

        for (int i = 0; i < studenti.size(); i++) {
            Label studenteLabel = new Label(" " + studenti.get(i).cognome().toUpperCase() + " " + studenti.get(i).nome().toUpperCase());
            studenteLabel.setMaxWidth(Double.MAX_VALUE);
            studenteLabel.getStyleClass().add("cell");
            calendarGrid.add(studenteLabel, 0, i + 1);
            GridPane.setHalignment(studenteLabel, HPos.CENTER);
            GridPane.setValignment(studenteLabel, VPos.CENTER);
        }

        for (int i = 0; i < dayOfMonth; i++) {
            Label giornoLabel = new Label(String.valueOf(i + 1));
            giornoLabel.getStyleClass().add("cell");
            calendarGrid.add(giornoLabel, i + 1, 0);
            GridPane.setHalignment(giornoLabel, HPos.CENTER);
            GridPane.setValignment(giornoLabel, VPos.CENTER);
        }
    }

    // Mostra il pannello per aggiungere un'assenza
    @FXML
    public void showAddAssenzaPane() {
        studentChoiceBox.getSelectionModel().clearSelection();
        absenceDatePicker.setValue(LocalDate.now());
        mainPane.setDisable(true);
        inputPane.setVisible(true);
        mainPane.setEffect(new GaussianBlur());
    }

    // Mostra il mese precedente nel calendario
    public void mesePrecedente() {
        mese = mese.minusMonths(1);
        updateMonthLabel();
        //monthLabel.setText(meseItaliano(String.valueOf(mese.getMonth())));
        populateCalendar();
        loadAndRenderAbsences();
    }

    // Mostra il mese successivo nel calendario
    public void meseSuccessivo() {
        mese = mese.plusMonths(1);
        updateMonthLabel();
        //monthLabel.setText(meseItaliano(String.valueOf(mese.getMonth())));
        populateCalendar();
        loadAndRenderAbsences();
    }

    // Aggiorna la UI quando il database invia eventi
    @Override
    public void update(DatabaseEvent event) {
        switch (event.type()) {
            case ASSENZA_AGGIUNTA:
            case ASSENZA_GIUSTIFICATA:
            case ASSENZA_ELIMINATA:
                Platform.runLater(() -> {
                    populateCalendar();
                    loadAndRenderAbsences();
                    System.out.println("Calendario docente aggiornato via Observer: " + event.type());
                });
                break;
            default:
                break;
        }
    }
}
