package application.export;

import application.model.ValutazioneStudente;

import java.util.List;

public interface StudentEvaluationStrategy extends ExportStrategy<List<ValutazioneStudente>> {
    // Interfaccia specifica per la valutazione dello studente (lista di voti)
}