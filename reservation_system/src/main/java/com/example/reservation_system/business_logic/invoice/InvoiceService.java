package com.example.reservation_system.business_logic.invoice;

import org.springframework.stereotype.Service;
import com.example.reservation_system.business_logic.bookings.Booking;
import com.lowagie.text.DocumentException;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class InvoiceService {
    private final InvoiceRepository invoiceRepository;
    private final PdfInvoiceService pdfInvoiceService;

    
    public InvoiceService(InvoiceRepository invoiceRepository, PdfInvoiceService pdfInvoiceService) {
        this.invoiceRepository = invoiceRepository;
        this.pdfInvoiceService = pdfInvoiceService;
    }

    public Invoice findByPDFPath(String pdf_path) {
        return invoiceRepository.findByPDFPath(pdf_path)
           .orElseThrow(() -> new IllegalStateException("Not find by this pdf path" + pdf_path));
    }

    public Invoice findByIssued_at (LocalDateTime issued_at) {
        return invoiceRepository.findByIssued_at(issued_at)
                .orElseThrow(() -> new IllegalStateException("Not found by this issued " + issued_at));
    }

    public Invoice findByBooking_id(Long bookingId) {
        return invoiceRepository.findByBooking_Id(bookingId)
            .orElseThrow(() -> new IllegalStateException("Not find by booking ID" + bookingId));
    }

     /**
     * Creates a professional PDF invoice for the booking, saves it to disk, and persists the invoice record.
     * Idempotent: if an invoice already exists for this booking, returns it without creating a duplicate.
     */

     public Invoice createInvoiceForBooking(Booking booking) {
        Optional<Invoice> existing = invoiceRepository.findByBooking_Id(booking.getId());
        if (existing.isPresent()) {
            return existing.get();
        }

        LocalDate issueDate = LocalDate.now();
        String invoiceNumber = booking.getInvoice_no() != null && !booking.getInvoice_no().isBlank()
            ? booking.getInvoice_no()
            : "INV-" + booking.getId() + "-" + System.currentTimeMillis();

        try {
            String pdfPath = pdfInvoiceService.generateAndSavedPdf(booking, invoiceNumber, issueDate);
            Invoice invoice = new Invoice(booking , pdfPath , issueDate);
            return invoiceRepository.save(invoice);
        }catch (IOException | DocumentException e) {
            throw new IllegalStateException("Failed to generate invoice PDF for booking " + booking.getId() , e);
        }
     }
     
     /**
     * Returns the PDF bytes for an existing invoice (e.g. for download).
     */
     public byte[] getPdfBytesForInvoice(Invoice invoice) throws IOException {
        return java.nio.file.Files.readAllBytes(java.nio.file.Path.of(invoice.getPdf_path()));
     }

     /**
     * Generates PDF bytes on-the-fly for a booking (does not persist invoice). Useful for preview or when file is not stored.
     */

     public byte[] generatePdfBytesBooking(Booking booking) throws DocumentException , IOException {
        String invoiceNumber = booking.getInvoice_no() != null && !booking.getInvoice_no().isBlank()
            ? booking.getInvoice_no()
            : "INV-" + booking.getId();

        return pdfInvoiceService.generatedPdfBytes(booking, invoiceNumber, LocalDate.now());
     }
   
}
