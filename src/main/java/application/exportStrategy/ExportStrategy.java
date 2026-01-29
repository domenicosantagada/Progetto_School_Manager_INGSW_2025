package application.exportStrategy;

import java.io.File;

// Interfaccia generica per le strategie di esportazione dei dati
// generica perche puo essere usata per esportare diversi tipi di dati (ad esempio: voti, compiti, ecc.)
// T rappresenta il tipo di dato da esportare
public interface ExportStrategy<T> {
    void export(T data, File outputFile) throws Exception;
}