package com.example.reservation_system.business_logic.admin_dashboard;

import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.payments.PaymentRepository;
import com.example.reservation_system.business_logic.room.RoomRepository;
import com.example.reservation_system.model.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;


@ExtendWith(MockitoExtension.class)
@DisplayName("AdminDashboardService")

class AdminDashboardService {

    @Mock 
    private BookingRepository bookingRepository;

    @Mock 
    private PaymentRepository paymentRepository;

    @Mock 
    private AppUserRepository appUserRepository;

    @Mock 
    private RoomRepository roomRepository ;

    @Mock 
    private AdminBookingService adminBookingService;

    @Mock
    private AdminDashboardService adminDashboardService;

    @BeforeEach 
    void setUp() {
        adminDashboardService = new AdminDashboardService(
            bookingRepository , 
            paymentRepository ,
            appUserRepository ,
            roomRepository 
        );

          // Inject adminBookingService via reflection or setter if available; otherwise dashboard methods that call it need the mock.
        // AdminDashboardService has constructor with 4 args and @Autowired AdminBookingService - so we need to add a setter or use reflection.
        // Checking the class again: it has @Autowired private AdminBookingService adminBookingService and a 4-arg constructor.
        // So the 4-arg constructor doesn't set adminBookingService. So when getDashboardStatus() is called it doesn't use adminBookingService.
        // modifyBooking and cancelBooking delegate to adminBookingService. So for getDashboardStatus we only need the 4 mocks.
    }

    @Test 
    @DisplayName("getDashboardStatus returns map with expected keys")

    void getDashboardStatusReturnsExcpectedKeys () {
        when(bookingRepository.getTotalBookings()).thenReturn(10L);
        when(appUserRepository.getAllUsers()).thenReturn(5L);
        when(roomRepository.count()).thenReturn(20L);
        when(paymentRepository.getTotalRevenue()).thenReturn(BigDecimal.valueOf(5000));
        when(bookingRepository.getTotalBookedNights()).thenReturn(25L);

        Map<String, Object> status = adminDashboardService.getDashboardStatus();

        assertThat(status).containsKeys(
            "totalBookings" , "totalUsers" , "totalRevenue" , 
            "ADR" , "AR" , "RevPAR" , "occupancy"
        );

        assertThat(status.get("totalBookings")).isEqualTo(10L);
        assertThat(status.get("totalUsers")).isEqualTo(5L);
        assertThat(status.get("totalRevenue")).isEqualByComparingTo(BigDecimal.valueOf(5000));
    }

    @Test 
    @DisplayName("getDashboardStatus handles null total revenue and total booked nights")
    void getDashboardStatusHandlesNulls() {
        when(bookingRepository.getTotalBookings()).thenReturn(0L);
        when(appUserRepository.getAllUsers()).thenReturn(0L);
        when(roomRepository.count()).thenReturn(0L);
        when(paymentRepository.getTotalRevenue()).thenReturn(null);
        when(bookingRepository.getTotalBookedNights()).thenReturn(null);

        Map<String , Object> status = adminDashboardService.getDashboardStatus();

        assertThat(status.get("totalRevenue")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(status.get("ADR")).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(status.get("occupancy")).isEqualByComparingTo(BigDecimal.ZERO);
    }
}