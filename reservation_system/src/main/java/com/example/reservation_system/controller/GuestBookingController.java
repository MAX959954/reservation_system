package com.example.reservation_system.controller;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingService;
import com.example.reservation_system.business_logic.bookings.CreateBookingCmd;
import com.example.reservation_system.business_logic.bookings.BookingStatus;
import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/guest/bookings")
@PreAuthorize("hasRole('GUEST')")
public class GuestBookingController {

    private final BookingService bookingService;
    private final AppUserService appUserService;

    public GuestBookingController(BookingService bookingService, AppUserService appUserService) {
        this.bookingService = bookingService;
        this.appUserService = appUserService;
    }

    @PostMapping
    public ResponseEntity<BookingResponse> createBooking(@Valid @RequestBody CreateBookingRequest request) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Validate dates
        LocalDate checkIn = LocalDate.parse(request.getCheckIn());
        LocalDate checkOut = LocalDate.parse(request.getCheckOut());
        
        if (checkIn.isBefore(LocalDate.now().plusDays(1))) {
            throw new IllegalArgumentException("Check-in must be at least tomorrow");
        }
        
        if (checkOut.isBefore(checkIn) || checkOut.isEqual(checkIn)) {
            throw new IllegalArgumentException("Check-out must be after check-in");
        }

        // Validate room data consistency
        if (request.getRoomIds().size() != request.getAdults().size() || 
            request.getRoomIds().size() != request.getChildren().size()) {
            throw new IllegalArgumentException("Room IDs, adults, and children lists must have same size");
        }

        CreateBookingCmd cmd = new CreateBookingCmd(
            checkIn,
            checkOut,
            request.getRoomIds(),
            request.getAdults(),
            request.getChildren(),
            request.getTotalAmount(),
            request.getCurrency(),
            currentUser.getId()
        );
        
        Booking booking = bookingService.createBooking(cmd);
        return ResponseEntity.ok(new BookingResponse(booking));
    }

    @GetMapping("/my")
    public ResponseEntity<List<BookingResponse>> getMyBookings() {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Booking> bookings = bookingService.findByUserId(currentUser.getId());
        return ResponseEntity.ok(bookings.stream()
            .map(BookingResponse::new)
            .collect(Collectors.toList()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BookingResponse> getBooking(@PathVariable Long id) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingService.findById(id);
        
        // Security check: ensure user can only access their own bookings
        if (!booking.getAppUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied: You can only view your own bookings");
        }
        
        return ResponseEntity.ok(new BookingResponse(booking));
    }

    @PutMapping("/{id}")
    public ResponseEntity<BookingResponse> updateBooking(
            @PathVariable Long id, 
            @Valid @RequestBody UpdateBookingRequest request) {
        
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking existingBooking = bookingService.findById(id);
        
        // Security check: ensure user can only update their own bookings
        if (!existingBooking.getAppUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied: You can only update your own bookings");
        }

        // Only allow updates for bookings in PENDING_PAYMENT or RESERVED status
        BookingStatus currentStatus = existingBooking.getStatus();
        if (currentStatus != BookingStatus.PENDING_PAYMENT && 
            currentStatus != BookingStatus.RESERVED) {
            throw new IllegalStateException("Cannot update booking in " + currentStatus + " status");
        }

        LocalDate newCheckIn = LocalDate.parse(request.getCheckIn());
        LocalDate newCheckOut = LocalDate.parse(request.getCheckOut());
        
        Booking updatedBooking = bookingService.updateBooking(id, newCheckIn, newCheckOut);
        return ResponseEntity.ok(new BookingResponse(updatedBooking));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> cancelBooking(@PathVariable Long id) {
        // Get current authenticated user
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        AppUser currentUser = appUserService.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Booking booking = bookingService.findById(id);
        
        // Security check: ensure user can only cancel their own bookings
        if (!booking.getAppUser().getId().equals(currentUser.getId())) {
            throw new SecurityException("Access denied: You can only cancel your own bookings");
        }

        // Only allow cancellation for bookings that haven't checked in
        BookingStatus currentStatus = booking.getStatus();
        if (currentStatus == BookingStatus.CHECKED_IN || 
            currentStatus == BookingStatus.COMPLETED) {
            throw new IllegalStateException("Cannot cancel booking that is already checked in or completed");
        }

        bookingService.cancelBooking(id);
        return ResponseEntity.ok().build();
    }

    // DTOs
    public static class CreateBookingRequest {
        @jakarta.validation.constraints.NotBlank(message = "Check-in date is required")
        @jakarta.validation.constraints.Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
        private String checkIn;

        @jakarta.validation.constraints.NotBlank(message = "Check-out date is required")
        @jakarta.validation.constraints.Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
        private String checkOut;

        @jakarta.validation.constraints.NotNull(message = "Room IDs are required")
        @jakarta.validation.constraints.Size(min = 1, message = "At least one room is required")
        private List<Long> roomIds;

        @jakarta.validation.constraints.NotNull(message = "Adults count is required")
        @jakarta.validation.constraints.Size(min = 1, message = "At least one adult count is required")
        private List<Integer> adults;

        @jakarta.validation.constraints.NotNull(message = "Children count is required")
        private List<Integer> children;

        @jakarta.validation.constraints.NotNull(message = "Total amount is required")
        @jakarta.validation.constraints.DecimalMin(value = "0.01", message = "Total amount must be greater than 0")
        private BigDecimal totalAmount;

        @jakarta.validation.constraints.Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
        private String currency = "USD";

        // Getters and setters
        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
        
        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
        
        public List<Long> getRoomIds() { return roomIds; }
        public void setRoomIds(List<Long> roomIds) { this.roomIds = roomIds; }
        
        public List<Integer> getAdults() { return adults; }
        public void setAdults(List<Integer> adults) { this.adults = adults; }
        
        public List<Integer> getChildren() { return children; }
        public void setChildren(List<Integer> children) { this.children = children; }
        
        public BigDecimal getTotalAmount() { return totalAmount; }
        public void setTotalAmount(BigDecimal totalAmount) { this.totalAmount = totalAmount; }
        
        public String getCurrency() { return currency; }
        public void setCurrency(String currency) { this.currency = currency; }
    }

    public static class UpdateBookingRequest {
        @jakarta.validation.constraints.NotBlank(message = "Check-in date is required")
        @jakarta.validation.constraints.Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
        private String checkIn;

        @jakarta.validation.constraints.NotBlank(message = "Check-out date is required")
        @jakarta.validation.constraints.Pattern(regexp = "\\d{4}-\\d{2}-\\d{2}", message = "Date must be in yyyy-MM-dd format")
        private String checkOut;

        // Getters and setters
        public String getCheckIn() { return checkIn; }
        public void setCheckIn(String checkIn) { this.checkIn = checkIn; }
        
        public String getCheckOut() { return checkOut; }
        public void setCheckOut(String checkOut) { this.checkOut = checkOut; }
    }

    public static class BookingResponse {
        private Long id;
        private LocalDate checkIn;
        private LocalDate checkOut;
        private BigDecimal totalAmount;
        private String currency;
        private BookingStatus status;
        private LocalDate createdAt;
        private LocalDate updatedAt;
        private UserInfo user;

        public BookingResponse(Booking booking) {
            this.id = booking.getId();
            this.checkIn = booking.getCheck_in();
            this.checkOut = booking.getCheck_out();
            this.totalAmount = booking.getTotal_amount();
            this.currency = booking.getCurrency();
            this.status = booking.getStatus();
            this.createdAt = booking.getCreated_at();
            this.updatedAt = booking.getUpdated_at();
            this.user = new UserInfo(booking.getAppUser());
        }

        // Getters
        public Long getId() { return id; }
        public LocalDate getCheckIn() { return checkIn; }
        public LocalDate getCheckOut() { return checkOut; }
        public BigDecimal getTotalAmount() { return totalAmount; }
        public String getCurrency() { return currency; }
        public BookingStatus getStatus() { return status; }
        public LocalDate getCreatedAt() { return createdAt; }
        public LocalDate getUpdatedAt() { return updatedAt; }
        public UserInfo getUser() { return user; }
    }

    public static class UserInfo {
        private Long id;
        private String username;
        private String fullName;
        private String email;

        public UserInfo(AppUser user) {
            this.id = user.getId();
            this.username = user.getUsername();
            this.fullName = user.getFull_name();
            this.email = user.getEmail();
        }

        // Getters
        public Long getId() { return id; }
        public String getUsername() { return username; }
        public String getFullName() { return fullName; }
        public String getEmail() { return email; }
    }
}
