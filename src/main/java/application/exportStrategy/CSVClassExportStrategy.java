package application.exportStrategy;

import application.model.StudenteTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Strategia Concreta per l'esportazione dell'andamento di classe in formato CSV.
 */
public class CSVClassExportStrategy implements ClassEvaluationStrategy {

    @Override
    public void export(List<StudenteTable> studenti, File file) throws Exception {
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            // Intestazione del file CSV
            writer.append("Cognome;Nome;Data Valutazione;Voto\n");

            // Scrittura dei dati
            for (StudenteTable studente : studenti) {
                String votoString = studente.voto() == 0 ? "N.d." : String.valueOf(studente.voto());
                writer.append(String.format("%s;%s;%s;%s\n",
                        studente.cognome().toUpperCase(),
                        studente.nome().toUpperCase(),
                        studente.dataValutazione(),
                        votoString
                ));
            }

            System.out.println("CSV per la classe creato con successo!");

        } catch (IOException e) {
            throw new Exception("Errore durante la scrittura del file CSV.", e);
        }
    }

    // Potresti aggiungere un metodo getFile simile a quello del PDF se devi personalizzare il FileChooser.
}