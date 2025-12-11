package application.model;

// Modello per i compiti assegnati agli studenti

public record CompitoAssegnato(int id, String prof, String materia, String data, String descrizione, String classe) {
}
