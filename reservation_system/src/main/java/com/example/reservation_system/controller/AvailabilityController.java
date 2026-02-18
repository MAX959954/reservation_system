package com.example.reservation_system.controller;

import com.example.reservation_system.business_logic.room_inventory.RoomInventory;
import com.example.reservation_system.business_logic.room_inventory.RoomInventoryService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/availability")
@PreAuthorize("permitAll()")
public class AvailabilityController {
    private final RoomInventoryService roomInventoryService; 

    public AvailabilityController (RoomInventoryService roomInventoryService) {
        this.roomInventoryService = roomInventoryService;
    }

    @GetMapping("/check")
    public ResponseEntity<?> checkAvailability( 
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn, 
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut, 
        @RequestParam(required = false) Long roomId) {

        if (checkIn.isAfter(checkOut)){
            return ResponseEntity.badRequest().body("Check-in date must be before check-out date");
        }

        if (checkIn.isBefore(LocalDate.now())){
            return ResponseEntity.badRequest().body("Check-in date cannot be in past");
        }

        List<RoomInventory> availableInventory; 

        if(roomId != null) {
            availableInventory = roomInventoryService.findByRoomIdAndNightDateBetween(roomId, checkIn, checkOut)
            .stream()
            .filter(inventory -> inventory.getBooked_count() < inventory.getAllotment())
            .collect(Collectors.toList());  
        }else {
            availableInventory = roomInventoryService.findAvailableInventory(checkIn, checkOut);
        }

        return ResponseEntity.ok(new AvailabilityResponse(availableInventory , checkIn , checkOut));
    }

    @GetMapping("/rooms/{roomId}/available")
    public ResponseEntity<?> checkRoomAvailability(
        @PathVariable Long roomId, 
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkIn, 
        @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd") LocalDate checkOut
    ) {
        List<RoomInventory> roomInventory = roomInventoryService.findByRoomIdAndNightDateBetween(roomId, checkIn, checkOut); 

        boolean isAvailable = roomInventory.stream()
            .allMatch(inventory -> inventory.getBooked_count() < inventory.getAllotment());
            
        return ResponseEntity.ok(new RoomAvailabilityResponse(roomId, checkIn, checkOut, isAvailable, roomInventory));
    }

    public static class AvailabilityResponse {
        private List<RoomInventory> availableRooms; 
        private LocalDate checkIn; 
        private LocalDate checkOut; 
        private int totalAvailableRooms; 

        public AvailabilityResponse (List<RoomInventory> availableRooms , LocalDate checkIn , LocalDate checkOut  ) {
            this.availableRooms =  availableRooms; 
            this.checkIn = checkIn;
            this.checkOut = checkOut;
            this.totalAvailableRooms = (int) availableRooms.stream()
                .map(inventory -> inventory.getRoom().getId())
                .distinct() 
                .count();
        }

        public List<RoomInventory> getAvailableRooms() { return availableRooms; }
        public LocalDate getCheckIn() {return checkIn;}
        public LocalDate getCheckOut() {return checkOut;}
        public int getTotalAvailableRooms() {return totalAvailableRooms;}
    }

    public static class RoomAvailabilityResponse {
        private Long roomId; 
        private boolean available;
        private List<RoomInventory> inventoryDetails;
        private LocalDate checkIn;
        private LocalDate checkOut; 

        public RoomAvailabilityResponse (Long roomId , LocalDate checkIn , LocalDate checkOut , boolean available, List<RoomInventory> inventoryDetails) {
            this.roomId = roomId;
            this.available = available;
            this.inventoryDetails = inventoryDetails;
            this.checkIn = checkIn;
            this.checkOut = checkOut;
        }

        public Long getRoomId() {return roomId;}
        public boolean isAvailable() {return available;}
        public List<RoomInventory> getInventoryDetails() {return inventoryDetails;}
        public LocalDate getCheckIn() {return checkIn;}
        public LocalDate getCheckOut() {return checkOut;}
    }
}
