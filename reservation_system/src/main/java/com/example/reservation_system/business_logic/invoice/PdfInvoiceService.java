package com.example.reservation_system.business_logic.invoice;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * Generates professional PDF invoices using OpenPDF.
 */
@Service 
public class PdfInvoiceService {
    private static final String COMPANY_NAME = "Reservation Hotel";
    private static final String COMPANY_LINE = "123 Hospitality Ave · support@reservation-hotel.com";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMM yyyy");
    private static final float MARGIN = 36f;

    @Value("${app.invoices.dir:${user.dir}/invoices}")
    private String invoicesBaseDir;

    /*
    Generates a PDF invoice for the given booking and returns the PDF as bytes.
     */

    public byte[] generatedPdfBytes(Booking booking, String invoiceNumber, LocalDate issueDate) throws DocumentException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            buildDocument(booking, invoiceNumber, issueDate, out);
        } catch (IOException e) {
            throw new com.lowagie.text.DocumentException(e);
        } catch (DocumentException e) {
            throw e;
        }
        return out.toByteArray();
    }

    /* * Generates a PDF invoice, saves it to the configured directory, and returns the relative path.*/

    public String generateAndSavedPdf(Booking booking , String invoiceNumber , LocalDate issueDate) throws IOException , DocumentException {
        Path basePath = Path.of(invoicesBaseDir);
        Files.createDirectories(basePath);

        String safeFileName = "invoice" + booking.getId() + " - " + System.currentTimeMillis() + ".pdf";
        Path filePath = basePath.resolve(safeFileName);

        try (ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            buildDocument(booking , invoiceNumber , issueDate , out);
            Files.write(filePath , out.toByteArray());
        }

        return filePath.toString();
    }

    private void buildDocument(Booking booking , String invoiceNumber , LocalDate issueDate, ByteArrayOutputStream out)throws DocumentException , IOException {
        Document document = new Document(PageSize.A4 , MARGIN , MARGIN , 50 , 40);
        PdfWriter writer = PdfWriter.getInstance(document, out);

        addMetadata(document , invoiceNumber);
        document.open();

        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD , 22 , Color.DARK_GRAY);
        Font headingFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12 , Color.DARK_GRAY);
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA , 10 , Color.BLACK);
        Font smallFont = FontFactory.getFont(FontFactory.HELVETICA,  9 , Color.GRAY);

        // Header: company name and tagline
        Paragraph company = new Paragraph(COMPANY_NAME , titleFont);
        company.setSpacingAfter(2);
        document.add(company);
        document.add(new Paragraph(COMPANY_LINE , smallFont));
        document.add(Chunk.NEWLINE);

        // Title and invoice details row (two columns)
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[] {1f , 1f});
        headerTable.setSpacingAfter(20);

        PdfPCell left = new PdfPCell(new Phrase("INVOICE" , FontFactory.getFont(FontFactory.HELVETICA_BOLD , 18)));
        left.setBorder(Rectangle.NO_BORDER);
        left.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(left);

        PdfPCell right = new PdfPCell();
        right.setBorder(Rectangle.NO_BORDER);
        right.setHorizontalAlignment(Element.ALIGN_LEFT);
        right.addElement(new Paragraph("Invoice No " + invoiceNumber , headingFont));
        right.addElement(new Paragraph("Issue date :" + issueDate.format(DATE_FORMAT) , normalFont));
        headerTable.addCell(right);

        document.add(headerTable);

        //Bill to 
        document.add(new Paragraph("Bill to" , headingFont));
        String guestName = booking.getAppUser().getFull_name() != null ? booking.getAppUser().getFull_name()  : booking.getAppUser().getUsername();
        String guestEmail = booking.getAppUser().getEmail() != null ? booking.getAppUser().getEmail() : "";
        document.add(new Paragraph(guestName , normalFont));
        document.add(new Paragraph(guestEmail , normalFont));
        document.add(Chunk.NEWLINE);

        //line items table 
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10);
        table.setSpacingAfter(15);

        addTableHeader(table , "Description" , "Check-in" , "Check-out" , "Amount");
        String description = "Accomodation - " + formatDate(booking.getCheck_in()) + " to " + formatDate(booking.getCheck_out());
        String amountStr = formatMoney(booking.getTotal_amount() , booking.getCurrency());
        table.addCell(cell(description , Element.ALIGN_LEFT));
        table.addCell(cell(formatDate(booking.getCheck_in()) , Element.ALIGN_LEFT));
        table.addCell(cell(formatDate(booking.getCheck_out()) , Element.ALIGN_LEFT));
        table.addCell(cell(amountStr , Element.ALIGN_RIGHT));

        document.add(table);

        //total 
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(40);
        totalTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalTable.setWidths(new float[] {1.5f , 1f});
        totalTable.getDefaultCell().setBorder(Rectangle.NO_BORDER);
        totalTable.addCell(cell("Total" , Element.ALIGN_RIGHT));
        totalTable.addCell(cell(amountStr , Element.ALIGN_RIGHT));
        document.add(totalTable);

        //footer 
        document.add(Chunk.NEWLINE);
        document.add(new Paragraph("Thank you for your reservation. This is your official invoice." , smallFont ));
        document.add(new Paragraph("For questions , contact support@reservation-hotel.com", smallFont));

        document.close();
    }

    private void addMetadata(Document document , String invoiceNumber) {
        document.addTitle("Invoice " + invoiceNumber  );
        document.addSubject("Reservation Invoice");
        document.addCreator(COMPANY_LINE);
    }


    private void addTableHeader(PdfPTable table, String... headers) {
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD , 10 , Color.WHITE);
        for(String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h , headerFont));
            cell.setBackgroundColor(new Color(60 , 60 , 60));
            cell.setHorizontalAlignment(Element.ALIGN_LEFT);
            cell.setPadding(6);
            table.addCell(cell);
        }
    }

    private PdfPCell cell (String text , int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(text , FontFactory.getFont(FontFactory.HELVETICA, 10 , Color.BLACK)));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMAT) : "-";
    }

    private String formatMoney(BigDecimal amount , String currency) {
        if(amount == null) return "0.00";
        String curr = currency != null ? currency.toUpperCase() : "USD";
        return String.format("%s %.2f", curr, amount);
    }

}
