package application.model;

public record ElaboratoCaricato(
        CompitoAssegnato compito,
        String studente,
        String data,
        String commento,
        byte[] file,
        int id //,
        //boolean approvato
) {
    // Costruttore per l'inserimento di nuovi elaborati (id generato dal DB)
    public ElaboratoCaricato(CompitoAssegnato compito, String studente, String data, String commento, byte[] file /*, boolean approvato*/) {
        this(compito, studente, data, commento, file, -1/*, approvato*/);
    }
}