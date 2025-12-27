package application.exportStrategy;

import application.Database;
import application.SceneHandler;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class ExportContext {

    private static ExportContext instance;
    private final Database database = Database.getInstance();
    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    private ExportStrategy<?> strategy;

    private ExportContext() {
    }

    public static ExportContext getInstance() {
        if (instance == null) {
            instance = new ExportContext();
        }
        return instance;
    }

    public void setStrategy(ExportStrategy<?> strategy) {
        this.strategy = strategy;
    }

    // Nuovo metodo helper per la finestra di dialogo di salvataggio
    private File getExportFile(String defaultFileName, String extensionDescription, String extension) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva File di Esportazione");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter(extensionDescription, extension));
        fileChooser.setInitialFileName(defaultFileName);
        return fileChooser.showSaveDialog(null);
    }

    @SuppressWarnings("unchecked")
    public void exportValutazione(List<ValutazioneStudente> voti) {
        if (strategy instanceof StudentEvaluationStrategy studentStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                // DEDUZIONE DEL FORMATO DALLA STRATEGIA
                boolean isCSV = strategy.getClass().getSimpleName().contains("CSV");
                String fileExtension = isCSV ? "*.csv" : "*.pdf";
                String extensionDescription = isCSV ? "CSV (Comma Separated Values)" : "PDF Document";
                String defaultFileName = nominativo + " - " + classe + "." + (isCSV ? "csv" : "pdf");


                // 1. Ottieni il percorso del file tramite FileChooser
                File file = getExportFile(defaultFileName, extensionDescription, fileExtension);

                // 2. Esegui l'esportazione delegando alla strategia
                studentStrategy.export(voti, file);

            } catch (Exception e) {
                System.out.println("Errore nel salvataggio del file: " + e.getMessage());
            }
        } else {
            System.out.println("Strategy non impostata o non corretta per la valutazione studente.");
        }
    }

    @SuppressWarnings("unchecked")
    public void exportAndamentoClasse(List<StudenteTable> studentiList) {
        if (strategy instanceof ClassEvaluationStrategy classStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                // DEDUZIONE DEL FORMATO DALLA STRATEGIA
                boolean isCSV = strategy.getClass().getSimpleName().contains("CSV");
                String fileExtension = isCSV ? "*.csv" : "*.pdf";
                String extensionDescription = isCSV ? "CSV (Comma Separated Values)" : "PDF Document";
                String defaultFileName = nominativo + " - Andamento classe " + classe + "." + (isCSV ? "csv" : "pdf");


                // 1. Ottieni il percorso del file tramite FileChooser
                File file = getExportFile(defaultFileName, extensionDescription, fileExtension);

                // 2. Esegui l'esportazione delegando alla strategia
                classStrategy.export(studentiList, file);

            } catch (Exception e) {
                System.out.println("Errore nel salvataggio del file: " + e.getMessage());
            }
        } else {
            System.out.println("Strategy non impostata o non corretta per l'andamento classe.");
        }
    }
}