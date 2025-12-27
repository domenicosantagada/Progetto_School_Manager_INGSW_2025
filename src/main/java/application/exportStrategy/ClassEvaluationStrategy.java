package application.exportStrategy;

import application.model.StudenteTable;

import java.util.List;

public interface ClassEvaluationStrategy extends ExportStrategy<List<StudenteTable>> {
    // Interfaccia specifica per l'andamento della classe (lista di StudenteTable)
}