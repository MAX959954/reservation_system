package com.example.reservation_system.business_logic.admin_dashboard;

import com.example.reservation_system.business_logic.booking_guest.BookinGuests;
import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.payments.PaymentRepository;
import com.example.reservation_system.business_logic.pricing.PricingService;

import org.assertj.core.api.OptionalAssert;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("AdminBookingService")
class AdminBookingService {

    @Mock 
    private BookingRepository bookingRepository;
    
    @Mock 
    private PaymentRepository paymentRepository;

    @Mock 
    private PricingService pricingService;

    @InjectMocks
    private AdminBookingService adminBookingService;

    private static final Long BOOKING_ID = 1L;
    private static final LocalDate NEW_CHECK_IN = LocalDate.of(2025 , 8 , 1);
    private static final LocalDate NEW_CHECK_OUT = LocalDate.of(2025 , 8 , 5);

    @Nested 
    @DisplayName("modifyBooking")
    class ModifyBooking {

        @Test
        @DisplayName("throws when booking not found")
        void  throwsWhenBookingNotFound() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminBookingService.modifyBooking(BOOKING_ID , NEW_CHECK_IN , NEW_CHECK_OUT))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Check-out must be after check-in");
        }

        @Test 
        @DisplayName("throws when check-out is before check-in")
        void throwsWhenCheclOutBeforeCheckIn() {
            Booking booking = new Booking();
            booking.setId(BOOKING_ID);
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

            assertThatThrownBy(() -> adminBookingService.modifyBooking(BOOKING_ID , NEW_CHECK_OUT , NEW_CHECK_IN))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Check-out must be after check-in");
        }

        @Test
        @DisplayName("updates dates and recalculate total from pricing service")
        void updatesDatesAndReculateTotal() {
            Booking booking = new Booking();
            booking.setId(BOOKING_ID);
            booking.setCheck_in(LocalDate.of(2025 , 7 , 1));
            booking.setCheck_out(LocalDate.of(2025 , 7 , 3));
            booking.setTotal_amount(BigDecimal.valueOf(100));

            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));
            BigDecimal newTotal = BigDecimal.valueOf(450);

            when(pricingService.calculateTotalForBooking(any(Booking.class))).thenReturn(newTotal);
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            Booking result = adminBookingService.modifyBooking(BOOKING_ID , NEW_CHECK_IN , NEW_CHECK_OUT);

            assertThat(result.getCheck_in()).isEqualTo(NEW_CHECK_IN);
            assertThat(result.getCheck_out()).isEqualTo(NEW_CHECK_OUT);
            assertThat(result.getTotal_amount()).isEqualByComparingTo(newTotal);
            verify(pricingService).calculateTotalForBooking(booking);
            verify(bookingRepository).save(booking);
        }
    }

    @Nested 
    @DisplayName("cancelBooking")
    class CancelBooking{

        @Test 
        @DisplayName("throws when booking not found")
        void throwsWhenBookingNotFound() {
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> adminBookingService.cancelBooking(BOOKING_ID))
                .isInstanceOfAny(IllegalStateException.class)
                .hasMessageContaining("Booking not found");
        }

        @Test
        @DisplayName("returns a refund amount when booking exists")
        void returnsRefundAmount() {
            Booking booking = new Booking();
            booking.setId(BOOKING_ID);
            booking.setCheck_in(LocalDate.now().plusDays(5));
            booking.setTotal_amount(BigDecimal.valueOf(300));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

            BigDecimal refund = adminBookingService.cancelBooking(BOOKING_ID);
            assertThat(refund).isNotNull();

            assertThat(refund).isEqualByComparingTo(BigDecimal.valueOf(300));
        }

        @Test
        @DisplayName("returns zero refund when check-in is in the past") 
        void returnsZeroWhenChecksInPast() {
            Booking booking = new Booking();
            booking.setId(BOOKING_ID);
            booking.setCheck_in(LocalDate.now().minusDays(1));
            booking.setTotal_amount(BigDecimal.valueOf(300));
            when(bookingRepository.findById(BOOKING_ID)).thenReturn(Optional.of(booking));

            BigDecimal refund = adminBookingService.cancelBooking(BOOKING_ID);

            assertThat(refund).isEqualByComparingTo(BigDecimal.ZERO);
        }

    }
}