package application.model;

public record ElaboratoCaricato(CompitoAssegnato compito, String studente, String data, String commento, byte[] file) {
}
