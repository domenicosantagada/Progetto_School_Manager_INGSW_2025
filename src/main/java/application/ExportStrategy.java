package application;

import java.io.File;

public interface ExportStrategy<T> {
    /**
     * Esegue l'esportazione dei dati nel formato specifico.
     *
     * @param data       I dati da esportare.
     * @param outputFile Il file di destinazione.
     * @throws Exception se l'esportazione fallisce.
     */
    void export(T data, File outputFile) throws Exception;
}
