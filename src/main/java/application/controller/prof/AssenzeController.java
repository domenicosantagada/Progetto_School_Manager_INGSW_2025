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
    private BorderPane updatePane; // Pannello per aggiungere una nuova assenza

    @FXML
    private Label meseLabel;

    @FXML
    private Label classeLabel;

    @FXML
    private GridPane calendarioGrid;

    // Variabili per tenere traccia del professore, classe e materia
    private String prof;
    private String classe;
    private String materia;

    // Lista degli studenti della classe
    private List<StudenteTable> studenti;

    // Mappa per tenere traccia della posizione di ogni studente nella griglia del calendario
    private final Map<StudenteTable, Integer> posizioneStudenti = new HashMap<>();

    // Variabile per tenere traccia del mese corrente
    private LocalDate mese;

    @FXML
    public void initialize() {
        // Viene aggiunto un "listener" (un osservatore) alla proprietà scene della classeLabel.
        // Poiché al momento dell'esecuzione di initialize la Scene (la finestra) potrebbe non
        // essere ancora completamente formata, questo codice attende che la scena venga assegnata.
        classeLabel.sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (newScene != null) {
                newScene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
            }
        });

        // Inizialmente nascondiamo il pannello di aggiunta di una nuova assenza
        updatePane.setVisible(false);

        // Impostiamo il mese corrente, il professore, la classe e la materia
        setMeseCorrente();
        setProfClasse();

        // Otteniamo la lista degli studenti della classe per la materia specifica
        // anche se la materia non viene utilizzata in questo contesto, viene comunque recuperata
        // per utilizzare lo stesso metodo di accesso al database che viene usato per
        // recuperare gli studenti in altri contesti.
        studenti = Database.getInstance().getStudentiClasse(classe, materia);

        // Mappiamo gli studenti alle loro posizioni nella griglia del calendario (studente -> indice)
        mappaStudenti();

        // Popoliamo il calendario con i giorni del mese e i nomi degli studenti
        populateCalendar();

        // Popoliamo la ChoiceBox con i nomi degli studenti
        for (StudenteTable s : studenti) {
            studenteChoice.getItems().add(s.cognome().toUpperCase() + " " + s.nome().toUpperCase());
        }

        // Impostiamo le assenze degli studenti nel calendario
        setAssenze();
    }

    private void mappaStudenti() {
        for (StudenteTable s : studenti) {
            posizioneStudenti.put(s, studenti.indexOf(s));
            /* Esempio di output della mappa
                s: CAPRARO posizione: 0
                s: LUZZI posizione: 1
                s: MAZZEI posizione: 2
                s: SANTAGADA posizione: 3
                s: SANTAGADA posizione: 4
             */
        }
    }

    private void setAssenze() {
        for (StudenteTable s : studenti) {
            // Otteniamo la lista delle assenze per lo studente nel mese corrente
            List<Assenza> assenzeStudente = Database.getInstance().getAssenzeStudente(s.username(), mese.getMonthValue());

            // Coloriamo le celle corrispondenti alle assenze nello schema del calendario
            for (Assenza a : assenzeStudente) {
                coloraCella(posizioneStudenti.get(s), a.giorno(), a.giustificata());
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
            // Controllo che i campi non siano vuoti, in caso contrario mostro un avviso all'utente
            if (studenteChoice.getSelectionModel().isEmpty() || dataAssenza.getValue() == null) {
                SceneHandler.getInstance().showWarning(MessageDebug.CAMPS_NOT_EMPTY);
            }

            // Ottengo la posizione dello studente selezionato nella griglia del calendario
            // Recupero l'indice selezionato nella ChoiceBox e lo uso per ottenere lo studente dalla lista
            Integer posizione = posizioneStudenti.get(studenti.get(studenteChoice.getSelectionModel().getSelectedIndex()));
            Integer giornoAssenza = dataAssenza.getValue().getDayOfMonth();
            StudenteTable studente = studenti.get(posizione);

            // Creo una nuova assenza e la aggiungo al database
            Assenza assenza = new Assenza(studente.username(), dataAssenza.getValue().getDayOfMonth(), dataAssenza.getValue().getMonthValue(), dataAssenza.getValue().getYear(), "Assenza non giustificata", false);
            Database.getInstance().addAssenza(assenza);

            // Coloro la cella corrispondente alla nuova assenza nel calendario
            coloraCella(posizione, giornoAssenza, false);

            // Torno al pannello principale
            // (opzionale: in questo caso se voglio aggiungere più assenze di seguito
            // devo riaprire il pannello di aggiunta assenza)
            backUpdateClicked();

        } catch (Exception e) {
            System.out.println("Errore nell'aggiunta dell'assenza");
        }
    }

    private void coloraCella(Integer posizione, Integer giorno, boolean giustificata) {
        // Usiamo sempre StackPane per centrare il testo
        StackPane cellaColorata = new StackPane();

        // Label per il testo (G, A, o altro)
        Label testoLabel = new Label();
        // Stile base per il testo: bianco e grassetto
        testoLabel.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");

        if (giustificata) {
            // --- CASO GIUSTIFICATA (BLU) ---
            cellaColorata.setStyle("-fx-background-color: blue;");
            testoLabel.setText("G"); // G per Giustificata
        } else {
            // --- CASO DA GIUSTIFICARE (ROSSO) ---
            cellaColorata.setStyle("-fx-background-color: red;");
            testoLabel.setText("A"); // A per Assenza (oppure usa "!")
        }

        // Aggiungo la label allo StackPane
        cellaColorata.getChildren().add(testoLabel);

        // Aggiungo la cella alla griglia
        calendarioGrid.add(cellaColorata, giorno, posizione + 1);
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

        // Inizializziamo il foglio di stile per la griglia del calendario
        Scene scene = calendarioGrid.getScene();
        if (scene != null) {
            scene.getStylesheets().add(getClass().getResource("/css/style.css").toExternalForm());
        }

        // Otteniamo il numero di giorni del mese corrente
        Integer dayOfMonth = mese.lengthOfMonth();

        // Ordiniamo gli studenti in ordine alfabetico per cognome prima di popolare la griglia
        studenti.sort((s1, s2) -> s1.cognome().compareToIgnoreCase(s2.cognome()));

        // Puliamo la griglia prima di popolarla
        calendarioGrid.getChildren().clear();
        calendarioGrid.getColumnConstraints().clear();
        calendarioGrid.getRowConstraints().clear();

        // Aggiungiamo la colonna per i nomi degli studenti
        ColumnConstraints studentColumn = new ColumnConstraints();
        studentColumn.setHgrow(Priority.NEVER); // La colonna degli studenti non si espande
        studentColumn.setPrefWidth(170); // Larghezza fissa per la colonna degli studenti
        calendarioGrid.getColumnConstraints().add(studentColumn); // Aggiungi la colonna per gli studenti

        // Aggiungiamo le colonne per i giorni del mese
        for (int i = 0; i < dayOfMonth; i++) {
            ColumnConstraints dayColumn = new ColumnConstraints();
            dayColumn.setHgrow(Priority.ALWAYS);  // Le colonne dei giorni si distribuiscono equamente
            dayColumn.setMinWidth(20); // Impostiamo una larghezza minima per i giorni
            calendarioGrid.getColumnConstraints().add(dayColumn); // Aggiungi la colonna per il giorno
        }

        // Aggiungiamo le righe per ogni studente più una riga per i giorni
        for (int i = 0; i < studenti.size() + 1; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.SOMETIMES); // La crescita verticale sarà uguale per tutte le righe
            calendarioGrid.getRowConstraints().add(row); // Aggiungi la riga alla griglia
        }

        // Popoliamo la griglia con i nomi degli studenti sulla prima colonna
        for (int i = 0; i < studenti.size(); i++) {
            // Creiamo una label per ogni studente con cognome e nome in maiuscolo
            Label studenteLabel = new Label(" " + studenti.get(i).cognome().toUpperCase() + " " + studenti.get(i).nome().toUpperCase());

            // Impostiamo la larghezza massima della label per occupare tutta la cella
            studenteLabel.setMaxWidth(Double.MAX_VALUE);

            // Aggiungiamo uno stile alla label
            studenteLabel.getStyleClass().add("cell");

            // Aggiungiamo la label dello studente alla griglia nella prima colonna (0) e nella riga corrispondente (i + 1) perché la prima riga è riservata ai giorni
            calendarioGrid.add(studenteLabel, 0, i + 1); // Aggiungi gli studenti alla prima colonna

            // Centra la label orizzontalmente e verticalmente
            GridPane.setHalignment(studenteLabel, HPos.CENTER);
            GridPane.setValignment(studenteLabel, VPos.CENTER);
        }

        // Popoliamo la griglia con i giorni del mese sulla prima riga
        for (int i = 0; i < dayOfMonth; i++) {
            // Creiamo una label per ogni giorno del mese
            Label giornoLabel = new Label(String.valueOf(i + 1));   // i + 1 perché i parte da 0 e i giorni da 1

            // Aggiungiamo uno stile alla label
            giornoLabel.getStyleClass().add("cell");

            // Aggiungiamo la label del giorno alla griglia nella riga 0 e nella colonna corrispondente (i + 1) perché la prima colonna è riservata agli studenti
            calendarioGrid.add(giornoLabel, i + 1, 0); // Aggiungi i giorni nella prima riga

            // Centra la label orizzontalmente e verticalmente
            GridPane.setHalignment(giornoLabel, HPos.CENTER);
            GridPane.setValignment(giornoLabel, VPos.CENTER);
        }
    }

    @FXML
    public void showAddAssenzaPane() {
        // Resettiamo i campi del pannello di aggiunta assenza
        studenteChoice.getSelectionModel().clearSelection();
        // Impostiamo la data corrente come data di assenza predefinita
        dataAssenza.setValue(LocalDate.now());

        // Mostriamo il pannello di aggiunta assenza e disabilitiamo il pannello principale con un effetto blur
        mainPane.setDisable(true);
        updatePane.setVisible(true);
        mainPane.setEffect(new GaussianBlur());
    }

    // Cambia il mese visualizzato nel calendario al mese precedente
    public void mesePrecedente() {
        mese = mese.minusMonths(1);
        String meseCorrente = meseItaliano(String.valueOf(mese.getMonth()));

        // Aggiorniamo la label del mese
        meseLabel.setText(meseCorrente);
        // Ripopoliamo il calendario con i giorni del mese aggiornato
        populateCalendar();
        // Aggiorniamo le assenze nel calendario
        setAssenze();
    }

    // Cambia il mese visualizzato nel calendario al mese successivo
    public void meseSuccessivo() {
        mese = mese.plusMonths(1);
        String meseCorrente = meseItaliano(String.valueOf(mese.getMonth()));

        // Aggiorniamo la label del mese
        meseLabel.setText(meseCorrente);
        // Ripopoliamo il calendario con i giorni del mese aggiornato
        populateCalendar();
        // Aggiorniamo le assenze nel calendario
        setAssenze();
    }
}
