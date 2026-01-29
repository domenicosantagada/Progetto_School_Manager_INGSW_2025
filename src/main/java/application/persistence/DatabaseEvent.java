package application.persistence;

// Modello per gli eventi del database
// Contiene il tipo di evento e i dati associati
public record DatabaseEvent(DatabaseEventType type, Object data) {
}