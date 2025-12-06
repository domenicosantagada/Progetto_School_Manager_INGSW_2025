package application.model;

public record ValutazioneStudente(String studente, String prof, String materia, String data, Integer voto) {
    public int compareTo(ValutazioneStudente valutazioneStudente) {
        return materia.compareTo(valutazioneStudente.materia);
    }
}
