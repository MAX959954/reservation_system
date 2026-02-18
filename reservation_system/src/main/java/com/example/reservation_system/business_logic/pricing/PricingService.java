package com.example.reservation_system.business_logic.pricing;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.booking_rooms.BookingRoomsRepository;
import com.example.reservation_system.business_logic.rates.Rates;
import com.example.reservation_system.business_logic.rates.RatesRepository;
import com.example.reservation_system.business_logic.room.RoomRepository;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Calculates seasonal rates and total amounts for bookings.
 * Uses the rates table (room_type + start_date/end_date) to resolve price per night.
 */
@Service
public class PricingService {
    private final RatesRepository ratesRepository;
    private final BookingRoomsRepository bookingRoomsRepository;
    private final RoomRepository roomRepository;

    public PricingService(RatesRepository ratesRepository,
                         BookingRoomsRepository bookingRoomsRepository,
                         RoomRepository roomRepository) {
        this.ratesRepository = ratesRepository;
        this.bookingRoomsRepository = bookingRoomsRepository;
        this.roomRepository = roomRepository;
    }

    /**
     * Returns the seasonal rate (price per night) for the given room type on the given date.
     * @throws IllegalStateException if no rate is defined for that room type and date
     */

    public int getSeasonRatePerNightPrice(String roomType, LocalDate nightDate) {
        List<Rates> applicable = ratesRepository.findApplicableRatesForDate(roomType, nightDate);
        if (applicable.isEmpty()) {
            throw new IllegalStateException(
                "No seasonal rate defined for that room type: " + roomType + " " + nightDate
            );
        }
        return applicable.get(0).getPrice();
    }


      /**
     * Calculates the total amount for a stay: for each night from check-in (inclusive) to
     * check-out (exclusive), and for each room type, adds the seasonal rate.
     * So total = sum over (each night × each room) of getSeasonalRateForNight(roomType, night).
     */

      public BigDecimal calculateTotalAmount (LocalDate checkIn, LocalDate checkOut , List<String> roomTypes) {
            if (checkOut == null || !checkOut.isAfter(checkIn)) {
                return BigDecimal.ZERO;
            }

            if (roomTypes == null || roomTypes.isEmpty()) {
                return BigDecimal.ZERO;
            }
            BigDecimal total = BigDecimal.ZERO;
            LocalDate night = checkIn;

            while (night.isBefore(checkOut)) {
                for (String roomType : roomTypes) {
                    int price = getSeasonRatePerNightPrice(roomType, night);
                    total = total.add(BigDecimal.valueOf(price));
                }
                night = night.plusDays(1);
            }
            return total ;
      }

    /**
     * Calculates the total amount for an existing booking based on its check-in, check-out,
     * and the room types of its booking_rooms (seasonal rates applied per night).
     */

    public BigDecimal calculateTotalForBooking(Booking booking) {
        List<String> roomTypes = bookingRoomsRepository.findByBookingId(booking.getId()).stream()
            .map(br -> br.getRoom() != null ? br.getRoom().getType() : null)
            .filter(type -> type != null)
            .collect(Collectors.toList());

        if (roomTypes.isEmpty()) {
            return BigDecimal.ZERO;
        }
        return calculateTotalAmount(booking.getCheck_in() , booking.getCheck_out() , roomTypes);
    }

    public BigDecimal calculateTotalForRoomIds(LocalDate checkIn, LocalDate checkOut, List<Long> roomIds) {
        if (roomIds == null || roomIds.isEmpty()) {
            return BigDecimal.ZERO;
        }
        List<String> roomTypes = roomIds.stream()
            .map(roomRepository::findById)
            .filter(opt -> opt.isPresent())
            .map(opt -> opt.get().getType())
            .collect(Collectors.toList());

        return calculateTotalAmount(checkIn , checkOut , roomTypes);
    }


}
