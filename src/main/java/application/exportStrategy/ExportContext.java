package application.exportStrategy;

import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import application.persistence.Database;
import application.view.SceneHandler;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

// Classe Context per gestire l'esportazione utilizzando diverse strategie di esportazione (PDF, CSV, ecc.)
// E' implementata come Singleton per garantire un'unica istanza in tutta l'applicazione.
public class ExportContext {

    private static ExportContext instance;
    private final Database database = Database.getInstance();
    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    private ExportStrategy<?> strategy;

    private ExportContext() {
    }

    // Restituisce l'istanza singleton di ExportContext
    public static ExportContext getInstance() {
        if (instance == null) {
            instance = new ExportContext();
        }
        return instance;
    }

    // Imposta la strategia di esportazione
    public void setStrategy(ExportStrategy<?> strategy) {
        System.out.println("Strategia di esportazione impostata su: " + strategy.getClass().getSimpleName());
        this.strategy = strategy; // qui stiamo assegnando la strategia passata come parametro ovvero PDFStudenteStrategy, CSVStudenteStrategy, PDFClasseStrategy o CSVClasseStrategy
    }

    // Mostra la finestra di dialogo per scegliere il file di esportazione
    private File getExportFile(String defaultFileName, String extensionDescription, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva File di Esportazione");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, extension));
        fileChooser.setInitialFileName(defaultFileName);
        return fileChooser.showSaveDialog(null);
    }

    // Esegue l'esportazione dei voti di uno studente
    @SuppressWarnings("unchecked") // Per evitare warning di cast non sicuri
    public void exportValutazione(List<ValutazioneStudente> voti) {

        // Verifica che la strategia sia di tipo ExportVotiStudente e facciamo il cast direttamente
        if (strategy instanceof ExportVotiStudente studentStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                // Determina se la strategia è per CSV o PDF
                boolean isCSV = strategy.getClass().getSimpleName().contains("CSV");

                // Imposta l'estensione del file e il nome di default
                String fileExtension = isCSV ? "*.csv" : "*.pdf";

                // Descrizione dell'estensione del file
                String extensionDescription = isCSV ? "CSV (Comma Separated Values)" : "PDF Document";

                // Nome di default del file
                String defaultFileName = nominativo + " - " + classe + "." + (isCSV ? "csv" : "pdf");

                // Ottiene il file di esportazione tramite la finestra di dialogo
                File file = getExportFile(defaultFileName, extensionDescription, fileExtension);

                // Esegue l'esportazione utilizzando la strategia selezionata
                studentStrategy.export(voti, file);

            } catch (Exception e) {
                System.out.println("Errore nel salvataggio del file: " + e.getMessage());
            }
        } else {
            System.out.println("Strategy non impostata o non corretta per la valutazione studente.");
        }
    }

    // Esegue l'esportazione dell'andamento della classe
    @SuppressWarnings("unchecked")
    public void exportAndamentoClasse(List<StudenteTable> studentiList) {
        if (strategy instanceof ExportVotiClasse classStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                boolean isCSV = strategy.getClass().getSimpleName().contains("CSV");
                String fileExtension = isCSV ? "*.csv" : "*.pdf";
                String extensionDescription = isCSV ? "CSV (Comma Separated Values)" : "PDF Document";
                String defaultFileName = nominativo + " - Andamento classe " + classe + "." + (isCSV ? "csv" : "pdf");

                File file = getExportFile(defaultFileName, extensionDescription, fileExtension);
                classStrategy.export(studentiList, file);

            } catch (Exception e) {
                System.out.println("Errore nel salvataggio del file: " + e.getMessage());
            }
        } else {
            System.out.println("Strategy non impostata o non corretta per l'andamento classe.");
        }
    }
}