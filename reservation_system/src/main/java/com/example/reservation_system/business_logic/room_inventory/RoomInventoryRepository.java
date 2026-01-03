package com.example.reservation_system.business_logic.room_inventory;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory , Long> {
    Optional<RoomInventory> findByNightDate(LocalDateTime night_date);
    Optional<RoomInventory> findByBookedCount(int booked_count);
}
