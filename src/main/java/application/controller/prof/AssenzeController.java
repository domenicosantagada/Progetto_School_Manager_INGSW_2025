package application.controller.prof;

import application.Database;
import application.MessageDebug;
import application.SceneHandler;
import application.model.Assenza;
import application.model.StudenteTable;
import javafx.fxml.FXML;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.Scene;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.*;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AssenzeController {

    @FXML
    private ChoiceBox studenteChoice;

    @FXML
    private DatePicker dataAssenza;

    @FXML
    private BorderPane mainPane;

    @FXML
    private BorderPane updatePane;

    @FXML
    private Label meseLabel;

    @FXML
    private Label classeLabel;

    @FXML
    private GridPane calendarioGrid;

    private String prof;
    private String classe;
    private String materia;
    private List<StudenteTable> studenti;
    private final Map<StudenteTable, Integer> posizioneStudenti = new HashMap<>();
    private LocalDate mese;

    @FXML
    public void initialize() {
        // Aggiungere un listener per la scena
        classeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }
        });

        updatePane.setVisible(false);
        setMeseCorrente();
        setProfClasse();
        studenti = Database.getInstance().getStudentiClasse(classe, materia);
        mappaStudenti();
        populateCalendar();
        for (StudenteTable s : studenti) {
            studenteChoice.getItems().add(s.cognome().toUpperCase() + " " + s.nome().toUpperCase());
        }
        setAssenze();

    }

    private void mappaStudenti() {
        for (StudenteTable s : studenti) {
            posizioneStudenti.put(s, studenti.indexOf(s));
        }
    }

    private void setAssenze() {
        for (StudenteTable s : studenti) {
            List<Assenza> assenzeStudente = Database.getInstance().getAssenzeStudente(s.username(), mese.getMonthValue());
            for (Assenza a : assenzeStudente) {
                Integer giornoAssenza = a.giorno();
                coloraCella(posizioneStudenti.get(s), giornoAssenza);
            }
        }
    }


    private void setMeseCorrente() {
        mese = LocalDate.now();
        String meseCorrente = meseItaliano(String.valueOf(mese.getMonth()));
        meseLabel.setText(meseCorrente);
    }

    private void setProfClasse() {
        prof = SceneHandler.getInstance().getUsername();
        classe = Database.getInstance().getClasseUser(prof);
        materia = Database.getInstance().getMateriaProf(prof);
        classeLabel.setText(classe);
    }

    private String meseItaliano(String s) {
        switch (s) {
            case "JANUARY":
                return "Gennaio";
            case "FEBRUARY":
                return "Febbraio";
            case "MARCH":
                return "Marzo";
            case "APRIL":
                return "Aprile";
            case "MAY":
                return "Maggio";
            case "JUNE":
                return "Giugno";
            case "JULY":
                return "Luglio";
            case "AUGUST":
                return "Agosto";
            case "SEPTEMBER":
                return "Settembre";
            case "OCTOBER":
                return "Ottobre";
            case "NOVEMBER":
                return "Novembre";
            case "DECEMBER":
                return "Dicembre";
            default:
                return "Mese non valido";
        }
    }

    @FXML
    private void addAssenzaClicked() {
        try {
            if (studenteChoice.getSelectionModel().isEmpty() || dataAssenza.getValue() == null) {
                SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            }
            // voglio ottenere la posizione dello studente selezionato
            Integer posizione = posizioneStudenti.get(studenti.get(studenteChoice.getSelectionModel().getSelectedIndex()));
            Integer giornoAssenza = dataAssenza.getValue().getDayOfMonth();
            StudenteTable studente = studenti.get(posizione);

            Assenza assenza = new Assenza(studente.username(), dataAssenza.getValue().getDayOfMonth(), dataAssenza.getValue().getMonthValue(), dataAssenza.getValue().getYear());
            Database.getInstance().addAssenza(assenza);
            coloraCella(posizione, giornoAssenza);
        } catch (Exception e) {
            System.out.println("Errore nell'aggiunta dell'assenza");
        }
    }

    private void coloraCella(Integer posizione, Integer giorno) {
        // Esempio coordinata della matrice 2 2 (riga 2 colonna 2) la coloro di rosso
        Pane cellaRossa = new Pane(); // Usare un `Pane` vuoto per rappresentare la cella
        cellaRossa.setStyle("-fx-background-color: red;"); // Imposta il colore di sfondo a rosso
        calendarioGrid.add(cellaRossa, giorno, posizione + 1); // Aggiungi la cella alla griglia
    }

    // Torno alla home page del professore
    @FXML
    private void backButtonClicked() throws IOException {
        SceneHandler.getInstance().setProfessorHomePage(prof);
    }

    // Torno indietro alla pagina delle assenze
    @FXML
    private void backUpdateClicked() {
        updatePane.setVisible(false);
        mainPane.setVisible(true);
        mainPane.setEffect(null);
        mainPane.setDisable(false);
    }


    private void populateCalendar() {

        Scene scene = calendarioGrid.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }

        Integer dayOfMonth = mese.lengthOfMonth();
        // voglio ordinare la lista degli studenti per cognome da A a Z ignorando le lettere maiuscole e minuscole
        studenti.sort((s1, s2) -> s1.cognome().compareToIgnoreCase(s2.cognome()));

        calendarioGrid.getChildren().clear();
        calendarioGrid.getColumnConstraints().clear();
        calendarioGrid.getRowConstraints().clear();

        // Impostiamo la ColumnConstraints per la colonna degli studenti
        ColumnConstraints studentColumn = new ColumnConstraints();
        studentColumn.setHgrow(Priority.NEVER); // La colonna degli studenti non si espande
        studentColumn.setPrefWidth(170); // Larghezza fissa per la colonna degli studenti
        calendarioGrid.getColumnConstraints().add(studentColumn); // Aggiungi la colonna per gli studenti

        // Aggiungiamo le colonne per i giorni (distribuzione equa della larghezza)
        for (int i = 0; i < dayOfMonth; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setHgrow(Priority.ALWAYS);  // Le colonne dei giorni si distribuiscono equamente
            dayColumn.setMinWidth(20); // Impostiamo una larghezza minima per i giorni
            calendarioGrid.getColumnConstraints().add(dayColumn);
        }

        // Aggiungiamo le righe per gli studenti
        for (int i = 0; i < studenti.size() + 1; i++) { // +1 per la riga dei giorni
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.SOMETIMES); // La crescita verticale sarà uguale per tutte le righe
            calendarioGrid.getRowConstraints().add(row);
        }

        // Popoliamo la griglia con i nomi degli studenti sulla prima colonna
        for (int i = 0; i < studenti.size(); i++) {
            Label studenteLabel = new Label(" " + studenti.get(i).cognome().toUpperCase() + " " + studenti.get(i).nome().toUpperCase());
            studenteLabel.setMaxWidth(Double.MAX_VALUE); // Imposta la larghezza massima per adattarsi al contenuto
            studenteLabel.getStyleClass().add("cell"); // Applica uno stile (opzionale)
            calendarioGrid.add(studenteLabel, 0, i + 1); // Aggiungi gli studenti alla prima colonna

            // Centra la label orizzontalmente e verticalmente
            GridPane.setHalignment(studenteLabel, HPos.CENTER);
            GridPane.setValignment(studenteLabel, VPos.CENTER);
        }

        // Popoliamo la griglia con i giorni del mese sulla prima riga
        for (int i = 0; i < dayOfMonth; i++) {
            Label giornoLabel = new Label(String.valueOf(i + 1));
            giornoLabel.getStyleClass().add("cell"); // Applica uno stile (opzionale)
            calendarioGrid.add(giornoLabel, i + 1, 0); // Aggiungi i giorni nella prima riga

            // Centra la label orizzontalmente e verticalmente
            GridPane.setHalignment(giornoLabel, HPos.CENTER);
            GridPane.setValignment(giornoLabel, VPos.CENTER);
        }
    }

    @FXML
    public void showAddAssenzaPane() {
        studenteChoice.getSelectionModel().clearSelection();
        dataAssenza.setValue(LocalDate.now());

        mainPane.setDisable(true);
        updatePane.setVisible(true);
        mainPane.setEffect(new GaussianBlur());
    }

    public void mesePrecedente() {
        mese = mese.minusMonths(1);
        String meseCorrente = meseItaliano(String.valueOf(mese.getMonth()));
        //aggiorniamo la label del mese
        meseLabel.setText(meseCorrente);
        populateCalendar();
        setAssenze();
    }

    public void meseSuccessivo() {
        mese = mese.plusMonths(1);
        String meseCorrente = meseItaliano(String.valueOf(mese.getMonth()));
        //aggiorniamo la label del mese
        meseLabel.setText(meseCorrente);
        populateCalendar();
        setAssenze();
    }
}
