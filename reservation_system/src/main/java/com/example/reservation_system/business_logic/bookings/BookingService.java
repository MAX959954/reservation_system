package com.example.reservation_system.business_logic.bookings;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;

    public BookingService(BookingRepository bookingRepository) {
        this.bookingRepository = bookingRepository;
    }

    public Booking findById(Long id){
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("No booking for that id"));
    }

    public Booking findByDateCheckIn (LocalDateTime check_in) {
        return bookingRepository.findByDateCheckIn(check_in)
                .orElseThrow(() -> new IllegalStateException("No booking found for check-in" + check_in));
    }

    public Booking findByCreatedAt(LocalDateTime created_at) {
        return bookingRepository.findByCreatedAt(created_at)
                .orElseThrow(() -> new IllegalStateException("No booking found for this " + created_at ));
    }

    public Booking CreateBooking (Booking booking) {
        booking.setCreated_at(LocalDateTime.now());
        booking.setUpdated_at(LocalDateTime.now());
        booking.setStatus(BookingStatus.RESERVED);
        return bookingRepository.save(booking);
    }

    public Booking updateBookingStatus (Long id , String status) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Booking not found by this id" + id));

        existing.setStatus(BookingStatus.valueOf(status));
        existing.setUpdated_at(LocalDateTime.now());
        return bookingRepository.save(existing);
    }

    public Booking updateBooking(Long bookingId , LocalDateTime newCheckIn , LocalDateTime newCheckedOut ) {
        Booking existingBooking =  bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Not found by this id" + bookingId));

        existingBooking.setCheck_in(newCheckIn);
        existingBooking.setCheck_out(newCheckedOut);
        existingBooking.setUpdated_at(LocalDateTime.now());

        return bookingRepository.save(existingBooking);
    }

    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)){
            throw new IllegalStateException("Booking not found by this Id" + id);
        }
        bookingRepository.deleteById(id);
    }

    public Booking cancelBooking(Long bookingId) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Not found by this id" + bookingId));

        existingBooking.setStatus(BookingStatus.CANCELLED);
        existingBooking.setUpdated_at(LocalDateTime.now());
        return bookingRepository.save(existingBooking);
    }

    public List<Booking> findRoomsBookedBetween(LocalDateTime check_in, LocalDateTime check_out) {
        List<Booking> allBookings = bookingRepository.findAll();

        return allBookings.stream()
                .filter(booking ->
                        booking.getCheck_in().isBefore(check_out) &&
                                booking.getCheck_out().isAfter(check_in)
                )
                .collect(Collectors.toList());
    }

    public long getTotalBookings() {
        long totalBookings = bookingRepository.getTotalBookings();
        return totalBookings;
    }

}
