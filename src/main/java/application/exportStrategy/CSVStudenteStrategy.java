package application.exportStrategy;

import application.model.ValutazioneStudente;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// Strategia concreta per esportare la scheda di valutazione di uno studente in formato CSV.
public class CSVStudenteStrategy implements ExportVotiStudente {

    /**
     * Esporta la lista dei voti dello studente su un file CSV.
     * Ogni riga contiene: materia, data della valutazione e voto.
     */
    @Override
    public void export(List<ValutazioneStudente> voti, File file) throws Exception {
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {

            // Scrive l'intestazione del CSV
            writer.append("Materia;Data Valutazione;Voto\n");

            // Scrive i dati dei voti
            for (ValutazioneStudente voto : voti) {
                String votoString = voto.voto() == 0 ? "N.d." : String.valueOf(voto.voto());
                writer.append(String.format("%s;%s;%s\n",
                        voto.materia(),
                        voto.data(),
                        votoString
                ));
            }

            System.out.println("CSV dello studente creato con successo!");

        } catch (IOException e) {
            // Rilancia l'eccezione con messaggio chiaro
            throw new Exception("Errore durante la scrittura del file CSV.", e);
        }
    }
}