package com.example.reservation_system.business_logic.admin_dashboard;

//Aggregate and display data for rooms, users, and bookings.
//Generate reports (e.g., revenue, occupancy rate, etc.)

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")

public class AdminDashboardController {
    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController (AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @PutMapping("/bookings/{id}")
    public ResponseEntity<?> modifyBooking(
            @PathVariable Long id ,
            @RequestParam LocalDate checkIn ,
            @RequestParam LocalDate checkOut
    ) {
        return ResponseEntity.ok(
                adminDashboardService.modifyBooking(id , checkIn , checkOut)
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id){
        adminDashboardService.cancelBooking(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/dashboard")
    public ResponseEntity<Map<String , Object>> getDashBoardStatus () {
        Map<String, Object> status = adminDashboardService.getDashboardStatus();
        return ResponseEntity.ok(status);
    }


}
