package com.example.reservation_system.business_logic.bookings;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/admin/bookings")
@PreAuthorize("hasRole('ADMIN')")
public class BookingController {
    private BookingService bookingService;

    public BookingController (BookingService bookingService ){
        this.bookingService = bookingService;
    }

    @GetMapping
    public ResponseEntity<Long> getTotalBookings() {
        return ResponseEntity.ok(bookingService.getTotalBookings());
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<Booking> updateStatus(
            @PathVariable Long id,
            @RequestParam String status) {

        return ResponseEntity.ok(bookingService.updateBookingStatus(id, status));
    }


    @PutMapping("/{id}")
    public ResponseEntity<Booking> updateBooking(@PathVariable Long id , LocalDateTime newCheckIn , LocalDateTime newCheckedOut ){
        return ResponseEntity.ok(bookingService.updateBooking(id , newCheckIn , newCheckedOut ));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBooking(@PathVariable Long id) {
        bookingService.deleteBooking(id);
        return ResponseEntity.noContent().build();
    }

}
