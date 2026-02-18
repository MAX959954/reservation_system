package com.example.reservation_system.controller;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingService;
import com.example.reservation_system.business_logic.invoice.Invoice;
import com.example.reservation_system.business_logic.invoice.InvoiceService;
import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserService;
import com.lowagie.text.DocumentException;

import lombok.Getter;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

@RestController 
@RequestMapping("/api/invoices")
public class InvoiceController {
    private final InvoiceService invoiceService;
    private final BookingService bookingService; 
    private final AppUserService appUserService;

    public InvoiceController(InvoiceService invoiceService ,BookingService bookingService , AppUserService appUserService  ) {
        this.invoiceService = invoiceService;
        this.bookingService = bookingService;
        this.appUserService = appUserService;
    }

     /**
     * Download PDF invoice for a booking. If an invoice record exists, serves the stored file;
     * otherwise generates PDF on-the-fly (without persisting).
     * GET /api/invoices/booking/{bookingId}/pdf
     */

     @GetMapping("/booking/{bookingId}/pdf")
     @PreAuthorize("hasRole('GUEST') or hasRole('STAFF') or hasRole('ADMIN')")
     public ResponseEntity<byte[]> downloadInvoiceEntity(@PathVariable Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        ensureCanAccessBooking(booking);

        try {
            Invoice existing = invoiceService.findByBooking_id(bookingId);
            byte[] pdfBytes = invoiceService.getPdfBytesForInvoice(existing);
            String filename = "invoice-" + bookingId + ".pdf";
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_PDF);
            headers.setContentDispositionFormData("attachment" , filename);
            headers.setContentLength(pdfBytes.length);
            return new ResponseEntity<>(pdfBytes , headers , HttpStatus.OK);
        }catch (IOException | DocumentException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
     }

    /**
     * Generate and persist a PDF invoice for a booking. Idempotent.
     * POST /api/invoices/booking/{bookingId}/generate
     */

    @PostMapping("/booking/{bookingId}/generate")
    @PreAuthorize("hasRole('GUEST') or hasRole('STAFF') or hasRole('ADMIN')")
    public ResponseEntity<InvoiceResponse> generaInvoice(@PathVariable Long bookingId) {
        Booking booking = bookingService.findById(bookingId);
        ensureCanAccessBooking(booking);

        try {
            Invoice invoice = invoiceService.createInvoiceForBooking(booking);
            return ResponseEntity.ok(new InvoiceResponse(invoice.getId() , invoice.getPdf_path() , invoice.getIssued_at().toString()));
        }catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new InvoiceResponse(null , null , "Failed to generate invoice: " + e.getMessage()));
        }
    }

    private void ensureCanAccessBooking(Booking booking) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if  (auth == null || !auth.isAuthenticated()) {
            throw new org.springframework.security.access.AccessDeniedException("Not authenticated");
        }

        if (auth.getAuthorities().stream().anyMatch(a -> "ROLE_ADMIN".equals(a.getAuthority()) || "ROLE_STAFF".equals(a.getAuthority()))) {
            return ;
        }
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
        .orElseThrow(() -> new RuntimeException("User not found"));

        if(!booking.getAppUser().getId().equals(currentUser.getId())){
            throw new org.springframework.security.access.AccessDeniedException("Not allowed to access this booking");
        }
    }

    public static class InvoiceResponse {
        @Getter
        private final Long invoiceId;
        @Getter
        private final String pdfPath;
        @Getter
        private final String issuedAtOrError;

        public InvoiceResponse (Long invoiceId ,  String pdfPath , String issuedAtOrError) {
            this.invoiceId = invoiceId;
            this.pdfPath = pdfPath;
            this.issuedAtOrError = issuedAtOrError;
        }
    }

}
