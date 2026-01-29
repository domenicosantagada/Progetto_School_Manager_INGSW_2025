package application.exportStrategy;

import application.model.ValutazioneStudente;
import application.persistence.Database;
import application.view.SceneHandler;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFStudenteStrategy implements ExportVotiStudente {

    private final Database database = Database.getInstance();
    private final SceneHandler sceneHandler = SceneHandler.getInstance();

    // Esporta le valutazioni dello studente in PDF
    @Override
    public void export(List<ValutazioneStudente> voti, File file) throws Exception {
        if (file == null)
            return;
        creaPDFValutazione(voti, file.getAbsolutePath());
    }

    // Crea il PDF con intestazione, tabella dei voti e footer
    private void creaPDFValutazione(List<ValutazioneStudente> voti, String outputPath) throws IOException, DocumentException {
        String username = sceneHandler.getUsername();
        String nominativo = database.getFullName(username);
        String classe = database.getClasseUser(username);
        String dataNascita = database.getDataNascita(username);

        // Ordina i voti per materia
        voti.sort(ValutazioneStudente::compareTo);

        Document document = new Document();
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));
        document.open();

        // Logo della scuola
        Image logo = Image.getInstance(getClass().getResource("/icon/logo.png"));
        logo.scaleToFit(100, 100);
        logo.setAlignment(Element.ALIGN_CENTER);
        document.add(logo);

        // Intestazione scuola
        Font schoolFont = new Font(Font.FontFamily.HELVETICA, 13, Font.ITALIC);
        Paragraph school = new Paragraph("Istituto Comprensivo Statale\nGiovanni Falcone", schoolFont);
        school.setAlignment(Element.ALIGN_CENTER);
        document.add(school);
        document.add(new Paragraph("\n\n"));

        // Titolo documento
        Font typeDocument = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD);
        Paragraph type = new Paragraph("SCHEDA DI VALUTAZINE", typeDocument);
        type.setAlignment(Element.ALIGN_CENTER);
        document.add(type);

        // Nome studente
        Font genericText = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph text = new Paragraph("di ", genericText);
        text.setAlignment(Element.ALIGN_CENTER);
        document.add(text);

        Paragraph student = new Paragraph(nominativo.toUpperCase(), typeDocument);
        student.setAlignment(Element.ALIGN_CENTER);
        document.add(student);

        Paragraph datiStudente = new Paragraph("nato/a il " + dataNascita + " e frequentante la classe " + classe, genericText);
        datiStudente.setAlignment(Element.ALIGN_CENTER);
        document.add(datiStudente);
        document.add(new Paragraph("\n\n"));

        // Tabella dei voti
        PdfPTable table = new PdfPTable(3); // Materia - Prof - Voto
        table.setWidths(new int[]{2, 2, 1});
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);
        BaseColor headerBgColor = new BaseColor(173, 216, 230); // Azzurro chiaro

        // Intestazioni tabella
        String[] headers = {"Materia", "Docente", "Voto"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerBgColor);
            cell.setPadding(10f);
            table.addCell(cell);
        }

        // Riempie la tabella con i voti
        for (ValutazioneStudente voto : voti) {
            PdfPCell cell = new PdfPCell(new Phrase(voto.materia(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(database.getFullName(voto.prof()).toUpperCase(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            String votoString = voto.voto() == 0 ? "N.d." : String.valueOf(voto.voto());
            cell = new PdfPCell(new Phrase(votoString, cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);
        }
        document.add(table);

        // Note esplicative
        Font noteFont = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC);
        Paragraph note = new Paragraph("* N.d. = Non sono ancora disponibili valutazioni per la materia da parte del docente.", noteFont);
        note.setAlignment(Element.ALIGN_LEFT);
        document.add(note);
        document.add(new Paragraph("\n\n\n\n"));

        // Footer con data generazione
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC, BaseColor.GRAY);
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Paragraph footer = new Paragraph("Documento generato in data: " + currentDate, footerFont);
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        document.close();
        System.out.println("PDF creato con successo! -> Pattern Strategy");
    }

    // Mostra dialogo per salvare il PDF
    public static File getFile(String nominativo, String classe, Window owner) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(nominativo + " - " + classe + ".pdf");
        return fileChooser.showSaveDialog(owner);
    }
}