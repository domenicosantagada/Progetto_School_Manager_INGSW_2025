package application.exportStrategy;

import application.model.ValutazioneStudente;

import java.util.List;

public interface ExportVotiStudente extends ExportStrategy<List<ValutazioneStudente>> {
    // Interfaccia specifica per la valutazione dello studente
    // Passiamo una lista di ValutazioneStudente da esportare
}