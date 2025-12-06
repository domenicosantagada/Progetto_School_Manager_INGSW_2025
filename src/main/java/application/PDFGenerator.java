package application;

import application.model.StudenteTable;
import application.model.ValutazioneStudente;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfWriter;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class PDFGenerator {

    private static PDFGenerator instance;

    public static PDFGenerator getInstance() {
        if (instance == null) {
            instance = new PDFGenerator();
        }
        return instance;
    }

    private PDFGenerator() {
    }

    private String username;        //username dell'utente loggato
    private String nominativo;      //nome e cognome dell'utente loggato
    private String classe;
    private List<ValutazioneStudente> voti;

    public void pdfValutazione(List<ValutazioneStudente> voti) {

        username = SceneHandler.getInstance().getUsername();
        nominativo = Database.getInstance().getFullName(username);
        classe = Database.getInstance().getClasseUser(username);
        //salvo la lista ordinata per nome della materia
        voti.sort(ValutazioneStudente::compareTo);
        this.voti = voti;

        // Faccio scegliere il percorso dove salvare il file attraverso un FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(nominativo + " - " + classe + ".pdf");

        // Apro il FileChooser e salvo il percorso del file
        try {
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                creaPDFValutazione(file.getAbsolutePath());
            }
        } catch (Exception e) {
            System.out.println("Errore nel salvataggio del file");
        }
    }

    private void creaPDFValutazione(String outputPath) throws IOException, DocumentException {
        // Crea il documento PDF
        Document document = new Document();

        // Crea un writer per scrivere nel file PDF
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));

        // Apri il documento per iniziare ad aggiungere contenuti
        document.open();

        // Aggiungo il logo della scuola centrato
        Image logo = Image.getInstance(getClass().getResource("/icon/logo.png"));
        logo.scaleToFit(100, 100);
        logo.setAlignment(Element.ALIGN_CENTER);
        document.add(logo);

        // Aggiungo l'intestazione della scuola al di sotto del logo
        Font schoolFont = new Font(Font.FontFamily.HELVETICA, 13, Font.ITALIC);
        Paragraph school = new Paragraph("Istituto Comprensivo Statale\n" +
                "Giovanni Falcone", schoolFont);
        school.setAlignment(Element.ALIGN_CENTER);
        document.add(school);

        //aggiungo spazio dopo l'intestazione
        document.add(new Paragraph("\n\n"));

        Font typeDocument = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD);
        Paragraph type = new Paragraph("SCHEDA DI VALUTAZINE", typeDocument);
        type.setAlignment(Element.ALIGN_CENTER);
        document.add(type);

        Font genericText = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph text = new Paragraph("di ", genericText);
        text.setAlignment(Element.ALIGN_CENTER);
        document.add(text);

        Font studentFont = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD);
        Paragraph student = new Paragraph(nominativo.toUpperCase(), typeDocument);
        student.setAlignment(Element.ALIGN_CENTER);
        document.add(student);

        Paragraph datiStudente = new Paragraph("nato/a il " + Database.getInstance().getDataNascita(username) + " e frequentante la classe " + classe, genericText);
        datiStudente.setAlignment(Element.ALIGN_CENTER);
        document.add(datiStudente);

        //aggiungo spazio dopo i dati dello studente
        document.add(new Paragraph("\n\n"));

        // Crea una tabella con 3 colonne
        PdfPTable table = new PdfPTable(3); // Materia - Prof - Voto
        table.setWidths(new int[]{2, 2, 1}); // Larghezza delle colonne
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Font per le intestazioni e le celle
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Colore di sfondo per l'intestazione
        BaseColor headerBgColor = new BaseColor(173, 216, 230); // Azzurro chiaro

        // Intestazioni della tabella
        String[] headers = {"Materia", "Docente", "Voto"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerBgColor);
            cell.setPadding(10f);
            table.addCell(cell);
        }

        // Dati della tabella
        for (ValutazioneStudente voto : voti) {
            // Cella materia
            PdfPCell cell = new PdfPCell(new Phrase(voto.materia(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            // Cella docente
            cell = new PdfPCell(new Phrase(Database.getInstance().getFullName(voto.prof()).toUpperCase(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            // Cella voto
            String votoString = voto.voto() == 0 ? "N.d." : String.valueOf(voto.voto());
            cell = new PdfPCell(new Phrase(votoString, cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);
        }
        document.add(table);

        //aggiungo note esplicative
        Font noteFont = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC);
        Paragraph note = new Paragraph("* N.d. = Non sono ancora disponibili valutazioni per la materia da parte del docente.", noteFont);
        note.setAlignment(Element.ALIGN_LEFT);
        document.add(note);

        //Aggiungo spazio dopo la tabella
        document.add(new Paragraph("\n\n\n\n"));

        // Font del footer personalizzato
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC, BaseColor.GRAY);
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Paragraph footer = new Paragraph(new Paragraph("Documento generato in data: " + currentDate, footerFont));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        // Chiudi il documento
        document.close();

        System.out.println("PDF creato con successo!");
    }

    public void pdfAndamentoClasse(List<StudenteTable> studentiList) {
        //ordino la lista per cognome
        studentiList.sort((s1, s2) -> s1.cognome().compareTo(s2.cognome()));
        System.out.println("PDF Andamento Classe");

        username = SceneHandler.getInstance().getUsername();
        nominativo = Database.getInstance().getFullName(username);
        classe = Database.getInstance().getClasseUser(username);

        // Faccio scegliere il percorso dove salvare il file attraverso un FileChooser
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Salva PDF");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("PDF", "*.pdf"));
        fileChooser.setInitialFileName(nominativo + " - Andamento classe " + classe + ".pdf");

        // Apro il FileChooser e salvo il percorso del file
        try {
            File file = fileChooser.showSaveDialog(null);
            if (file != null) {
                creaPDFAndamento(file.getAbsolutePath(), studentiList);
            }
        } catch (Exception e) {
            System.out.println("Errore nel salvataggio del file");
        }
    }

    private void creaPDFAndamento(String outputPath, List<StudenteTable> studenti) throws IOException, DocumentException {
        // Crea il documento PDF
        Document document = new Document();

        // Crea un writer per scrivere nel file PDF
        PdfWriter.getInstance(document, new FileOutputStream(outputPath));

        // Apri il documento per iniziare ad aggiungere contenuti
        document.open();

        // Aggiungo il logo della scuola centrato
        Image logo = Image.getInstance(getClass().getResource("/icon/logo.png"));
        logo.scaleToFit(100, 100);
        logo.setAlignment(Element.ALIGN_CENTER);
        document.add(logo);

        // Aggiungo l'intestazione della scuola al di sotto del logo
        Font schoolFont = new Font(Font.FontFamily.HELVETICA, 13, Font.ITALIC);
        Paragraph school = new Paragraph("Istituto Comprensivo Statale\n" +
                "Giovanni Falcone", schoolFont);
        school.setAlignment(Element.ALIGN_CENTER);
        document.add(school);

        //aggiungo spazio dopo l'intestazione
        document.add(new Paragraph("\n\n"));

        Font typeDocument = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD);
        Paragraph type = new Paragraph("ANDAMENTO CLASSE " + classe, typeDocument);
        type.setAlignment(Element.ALIGN_CENTER);
        document.add(type);

        document.add(new Paragraph("\n"));


        Font materiaFont = new Font(Font.FontFamily.HELVETICA, 17, Font.BOLD);
        Paragraph student = new Paragraph(Database.getInstance().getMateriaProf(username), materiaFont);
        student.setAlignment(Element.ALIGN_CENTER);
        document.add(student);

        Font genericText = new Font(Font.FontFamily.HELVETICA, 12);
        Paragraph text = new Paragraph("del docente ", genericText);
        text.setAlignment(Element.ALIGN_CENTER);
        document.add(text);

        Font profFont = new Font(Font.FontFamily.HELVETICA, 15, Font.BOLD);
        Paragraph docente = new Paragraph(nominativo.toUpperCase(), profFont);
        docente.setAlignment(Element.ALIGN_CENTER);
        document.add(docente);


        //aggiungo spazio dopo i dati dello studente
        document.add(new Paragraph("\n"));

        // Crea una tabella con 3 colonne
        PdfPTable table = new PdfPTable(3); // Materia - Prof - Voto
        table.setWidths(new int[]{2, 2, 1}); // Larghezza delle colonne
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Font per le intestazioni e le celle
        Font headerFont = new Font(Font.FontFamily.HELVETICA, 12, Font.BOLD);
        Font cellFont = new Font(Font.FontFamily.HELVETICA, 12);

        // Colore di sfondo per l'intestazione
        BaseColor headerBgColor = new BaseColor(173, 216, 230); // Azzurro chiaro

        // Intestazioni della tabella
        String[] headers = {"Studente", "Data Valutazione", "Voto"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerBgColor);
            cell.setPadding(10f);
            table.addCell(cell);
        }

        // Dati della tabella
        for (StudenteTable s : studenti) {
            // Cella materia
            PdfPCell cell = new PdfPCell(new Phrase(s.cognome().toUpperCase() + " " + s.nome().toUpperCase(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            cell = new PdfPCell(new Phrase(s.dataValutazione(), cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);

            // Cella voto
            String votoString = s.voto() == 0 ? "N.d." : String.valueOf(s.voto());
            cell = new PdfPCell(new Phrase(votoString, cellFont));
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setPadding(8f);
            table.addCell(cell);
        }
        document.add(table);

        //aggiungo note esplicative
        Font noteFont = new Font(Font.FontFamily.HELVETICA, 7, Font.ITALIC);
        Paragraph note = new Paragraph("* N.d. = Non sono ancora disponibili valutazioni per la materia da parte del docente.", noteFont);
        note.setAlignment(Element.ALIGN_LEFT);
        document.add(note);

        //Aggiungo spazio dopo la tabella
        document.add(new Paragraph("\n\n"));

        // Font del footer personalizzato
        Font footerFont = new Font(Font.FontFamily.HELVETICA, 10, Font.BOLDITALIC, BaseColor.GRAY);
        String currentDate = new SimpleDateFormat("dd/MM/yyyy").format(new Date());
        Paragraph footer = new Paragraph(new Paragraph("Documento generato in data: " + currentDate, footerFont));
        footer.setAlignment(Element.ALIGN_RIGHT);
        document.add(footer);

        // Chiudi il documento
        document.close();

        System.out.println("PDF creato con successo!");
    }
}