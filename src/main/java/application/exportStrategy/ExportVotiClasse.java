package application.exportStrategy;

import application.model.StudenteTable;

import java.util.List;

public interface ExportVotiClasse extends ExportStrategy<List<StudenteTable>> {
    // Interfaccia specifica per l'andamento della classe
    // Passiamo una lista di StudenteTable da esportare
}