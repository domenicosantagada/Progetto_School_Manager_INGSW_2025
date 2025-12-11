package application.model;

// Modello per le assenze degli studenti

public record Assenza(String username, Integer giorno, Integer mese, Integer anno, String motivazione,
                      boolean giustificata) {
}