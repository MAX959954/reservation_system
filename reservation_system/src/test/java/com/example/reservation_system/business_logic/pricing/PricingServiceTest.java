package com.example.reservation_system.business_logic.pricing;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.booking_rooms.BookingRooms;
import com.example.reservation_system.business_logic.booking_rooms.BookingRoomsRepository;
import com.example.reservation_system.business_logic.rates.Rates;
import com.example.reservation_system.business_logic.rates.RatesRepository;
import com.example.reservation_system.business_logic.room.Room;
import com.example.reservation_system.business_logic.room.RoomRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("PricingService")
class PricingServiceTest {
    @Mock 
    private RatesRepository ratesRepository;
    @Mock
    private BookingRoomsRepository bookingRoomsRepository;
    @Mock 
    private RoomRepository roomRepository;

    @InjectMocks
    private PricingService pricingService;

    private static final LocalDate CHECK_IN = LocalDate.of(2025, 6 , 1);
    private static final LocalDate CHECK_OUT = LocalDate.of(2025 , 6 , 3);

    @Nested 
    @DisplayName("calculateTotalAmount")
    class calculateTotalAmount {
        
        @Test
        @DisplayName("returns ZERO when check-out is before or equals to check-in")
        void returnZeroWhenInvalidDates() {
            assertThat(pricingService.calculateTotalAmount(CHECK_OUT , CHECK_IN , List.of("STAMDARD")))
                .isEqualByComparingTo(BigDecimal.ZERO);

            assertThat(pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of("STANDARD")))
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test 
        @DisplayName("returns ZERO when room types list is null or empty")
        void returnZeroWhenNoRoomType() {
            assertThat(pricingService.calculateTotalAmount(CHECK_OUT, CHECK_IN, null))
                .isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("throws when no rate defined for room type")
        void throwsWhenNoRateDefined() {
            when(ratesRepository.findApplicableRatesForDate(eq("UNKNOWN") , any(LocalDate.class)))
                .thenReturn(List.of());
            
            assertThatThrownBy(() -> pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of("UNKNOWN")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No seasonal rate defined");
        }

        @Test 
        @DisplayName("sums seasonal rate per night for each room type")
        void sumRatePerNight() {
            when(ratesRepository.findApplicableRatesForDate(eq("STANDARD"), eq(LocalDate.of(2025 , 6 , 1))))
                .thenReturn(List.of(new Rates("STANDARD" , CHECK_IN , CHECK_OUT , 100)));

            when(ratesRepository.findApplicableRatesForDate(eq("STANDARD"), eq(LocalDate.of(2025 , 6 , 2))))
                .thenReturn(List.of(new Rates("STANDARD" , CHECK_IN , CHECK_OUT , 100)));

            BigDecimal total = pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of("STANDARD"));

            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("sums multiple room type per night")
        void sumsMultipleRoomTypes () {
            when(ratesRepository.findApplicableRatesForDate(eq("STANDARD"), any(LocalDate.class)))
                .thenReturn(List.of(new Rates("STANDARD" , CHECK_IN , CHECK_OUT , 80)));
            
            when(ratesRepository.findApplicableRatesForDate(eq("DELUXE"), any(LocalDate.class)))
                .thenReturn(List.of(new Rates("DELUXE" , CHECK_IN , CHECK_OUT , 120)));

            BigDecimal total = pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of("STANDARD" , "DELUXE"));

            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(400));
        } 
    }

    @Nested 
    @DisplayName("calculateTotalForBooking")
    class calculateTotalForBooking {
        @Test 
        @DisplayName("returns ZERO when booking has no room types")
        void returnsZeroWhenNoRooms() {
            Booking existing = new Booking();
            existing.setId(1L);
            existing.setCheck_in(CHECK_IN);
            existing.setCheck_out(CHECK_OUT);
            when(bookingRoomsRepository.findByBookingId(1L)).thenReturn(List.of());

            assertThat(pricingService.calculateTotalForBooking(existing))
                .isEqualByComparingTo(BigDecimal.ZERO);
        }


        @Test 
        @DisplayName("delegates to calculateTotalAmount using room types from booking_rooms")
        void useRoomTypesForBookingRooms() {
            Booking booking = new Booking();
            booking.setId(1L);
            booking.setCheck_in(CHECK_IN);
            booking.setCheck_out(CHECK_OUT);
           
            Room room = new Room();
            room.setType("STANDARD");
            BookingRooms br = new BookingRooms();
            br.setRoom(room);
            when(bookingRoomsRepository.findByBookingId(1L)).thenReturn(List.of(br));
            when(ratesRepository.findApplicableRatesForDate(eq("STANDARD"),any(LocalDate.class) ))
                .thenReturn(List.of(new Rates("STANDARD" , CHECK_IN , CHECK_OUT , 99)));

            BigDecimal total = pricingService.calculateTotalForBooking(booking);
            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(198));  
        }
    }


    @Nested 
    @DisplayName("clalculateTotalForRoomIds")
    class CalculateTotalForRoomIds{

        @Test
        @DisplayName("returns Zero when roomdIds is null or empty")
        void returnZeroWhenNoRoomIds() {
            assertThat(pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, null))
                .isEqualByComparingTo(BigDecimal.ZERO);

            assertThat(pricingService.calculateTotalAmount(CHECK_IN, CHECK_OUT, List.of()))
                .isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test 
        @DisplayName("resolves room types from repository and calculates total") 
        void resolveRoomTypesAndCalculateTotal () {
            Room room = new Room();
            room.setId(1L);
            room.setType("STANDARD");
            when(roomRepository.findById(1L)).thenReturn(Optional.of(room));
            when(ratesRepository.findApplicableRatesForDate(eq("STANDARD"), any(LocalDate.class)))
                .thenReturn(List.of(new Rates("STANDARD" , CHECK_IN , CHECK_OUT , 50)));

            BigDecimal total = pricingService.calculateTotalForRoomIds(CHECK_IN, CHECK_OUT, List.of(1L));

            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(100));
        }
    }


}
