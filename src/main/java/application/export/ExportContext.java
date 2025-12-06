package application.export;


import application.Database;
import application.SceneHandler;
import application.model.StudenteTable;
import application.model.ValutazioneStudente;

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

    /**
     * Gestisce l'esportazione della valutazione dello studente (usato dagli studenti).
     */
    public void exportValutazione(List<ValutazioneStudente> voti) {
        // Verifica che la strategia sia del tipo corretto
        if (strategy instanceof StudentEvaluationStrategy studentStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                // 1. Ottieni il percorso del file tramite l'utility FileChooser (dalla strategia)
                File file = PDFExportStrategy.getFile(nominativo, classe, null);

                // 2. Esegui l'esportazione delegando alla strategia
                studentStrategy.export(voti, file);

            } catch (Exception e) {
                System.out.println("Errore nel salvataggio del file: " + e.getMessage());
            }
        } else {
            System.out.println("Strategy non impostata o non corretta per la valutazione studente.");
        }
    }

    /**
     * Gestisce l'esportazione dell'andamento di classe (usato dai docenti).
     */
    public void exportAndamentoClasse(List<StudenteTable> studentiList) {
        // Verifica che la strategia sia del tipo corretto
        if (strategy instanceof ClassEvaluationStrategy classStrategy) {
            try {
                String username = sceneHandler.getUsername();
                String nominativo = database.getFullName(username);
                String classe = database.getClasseUser(username);

                // 1. Ottieni il percorso del file tramite l'utility FileChooser (dalla strategia)
                File file = PDFClassExportStrategy.getFile(nominativo, classe, null);

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
