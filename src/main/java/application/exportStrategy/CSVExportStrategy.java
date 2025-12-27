package application.exportStrategy;

import application.model.ValutazioneStudente;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

/**
 * Strategia Concreta per l'esportazione della scheda di valutazione dello Studente in formato CSV.
 */
public class CSVExportStrategy implements StudentEvaluationStrategy {

    @Override
    public void export(List<ValutazioneStudente> voti, File file) throws Exception {
        if (file == null) return;

        try (FileWriter writer = new FileWriter(file)) {
            // Intestazione del file CSV
            writer.append("Materia;Data Valutazione;Voto\n");

            // Scrittura dei dati
            for (ValutazioneStudente voto : voti) {
                String votoString = voto.voto() == 0 ? "N.d." : String.valueOf(voto.voto());
                writer.append(String.format("%s;%s;%s\n",
                        voto.materia(),
                        voto.data(),
                        votoString
                ));
            }

            System.out.println("CSV per lo studente creato con successo!");

        } catch (IOException e) {
            throw new Exception("Errore durante la scrittura del file CSV.", e);
        }
    }

    // Potresti aggiungere un metodo getFile simile a quello del PDF se devi personalizzare il FileChooser.
}