package application.exportStrategy;

import application.model.StudenteTable;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;

// Strategia concreta per esportare l'andamento di una classe in formato CSV
public class CSVClasseStrategy implements ExportVotiClasse {

    /**
     * Esporta i dati degli studenti su file CSV.
     * Scrive cognome, nome, data valutazione e voto.
     */
    @Override
    public void export(List<StudenteTable> studenti, File file) throws Exception {
        if (file == null)
            return;

        try (FileWriter writer = new FileWriter(file)) {

            // Intestazione CSV
            writer.append("Cognome;Nome;Data Valutazione;Voto\n");

            // Scrive ogni studente nel CSV
            for (StudenteTable studente : studenti) {
                String votoString = studente.voto() == 0 ? "N.d." : String.valueOf(studente.voto());
                writer.append(String.format("%s;%s;%s;%s\n",
                        studente.cognome().toUpperCase(),
                        studente.nome().toUpperCase(),
                        studente.dataValutazione(),
                        votoString
                ));
            }

            System.out.println("CSV della classe creato con successo!");

        } catch (IOException e) {
            // Propaga l'eccezione con messaggio chiaro
            throw new Exception("Errore durante la scrittura del file CSV.", e);
        }
    }
}