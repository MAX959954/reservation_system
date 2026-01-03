package com.example.reservation_system.business_logic.admin_dashboard;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.payments.PaymentRepository;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.room.RoomRepository;
import com.example.reservation_system.model.AppUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Service
public class AdminDashboardService {
    @Autowired
    private BookingRepository bookingRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private  AdminBookingService adminBookingService;

    public AdminDashboardService(BookingRepository bookingRepository ,
                                 PaymentRepository paymentRepository ,
                                 AppUserRepository appUserRepository,
                                 RoomRepository roomRepository) {
        this.bookingRepository = bookingRepository;
        this.paymentRepository = paymentRepository;
        this.appUserRepository = appUserRepository;
        this.roomRepository = roomRepository;
    }

    public Map<String , Object> getDashboardStatus() {

        long totalBookings = bookingRepository.getTotalBookings();
        long totalUsers = appUserRepository.getAllUsers();
        long totalRooms = roomRepository.count();

        BigDecimal TotalRevenue = paymentRepository.getTotalRevenue() != null
                ? paymentRepository.getTotalRevenue()
                : BigDecimal.valueOf(0.0);

        long  totalBookedNights = bookingRepository.getTotalBookedNights() != null
                ? bookingRepository.getTotalBookedNights()
                : 0;

        //🔹 ADR = Total Revenue / Total Booked Nights
        BigDecimal adr =  totalBookedNights > 0
                ? TotalRevenue.divide(
                BigDecimal.valueOf(totalBookedNights),
                2,
                RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        BigDecimal averageRevenue = totalBookings > 0
                ? TotalRevenue.divide(
                BigDecimal.valueOf(totalBookings),
                2,
                RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        //🔹 Occupancy = (Total Booked Nights / (Total Rooms × Period Days)) × 100
        // Using a period of 365 days for calculation
        long periodDays = 365L;
        BigDecimal occupancy = totalRooms > 0 && periodDays > 0 ?
                BigDecimal.valueOf(totalBookedNights)
                        .divide(
                                BigDecimal.valueOf(totalRooms * periodDays),
                                4,
                                RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(100))
                        .setScale(2, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        //🔹 RevPAR = Total Revenue / (Total Rooms × Period Days)
        // Or alternatively: RevPAR = ADR × (Occupancy / 100)
        BigDecimal revPAR = totalRooms > 0 && periodDays > 0 ?
                TotalRevenue.divide(
                        BigDecimal.valueOf(totalRooms * periodDays),
                        2,
                        RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        Map<String , Object> status = new HashMap<>();
        status.put("totalBookings" , totalBookings);
        status.put("totalUsers" , totalUsers);
        status.put("totalRevenue" , TotalRevenue);
        status.put("ADR" , adr);
        status.put("AR" , averageRevenue);
        status.put("RevPAR" , revPAR);
        status.put("occupancy" , occupancy);
        return status;
    }

    public Booking modifyBooking(Long bookingId , LocalDate checkIn , LocalDate checkOut ){
        return adminBookingService.modifyBooking(bookingId , checkIn , checkOut);
    };

    public BigDecimal cancelBooking(Long bookingId) {
        return adminBookingService.cancelBooking(bookingId);
    }

}
