package com.example.reservation_system.business_logic.room_inventory;

import org.springframework.stereotype.Service;
import java.time.LocalDateTime;

@Service
public class RoomInventoryService {
    private final RoomInventoryRepository roomInventoryRepository ;

    public RoomInventoryService(RoomInventoryRepository roomInventoryRepository) {
        this.roomInventoryRepository = roomInventoryRepository;
    }

    public RoomInventory findByNightDate(LocalDateTime night_date) {
        return roomInventoryRepository.findByNightDate(night_date)
                .orElseThrow(() -> new IllegalStateException("Not found by this night date " + night_date ));
    }

    public RoomInventory  findByBookedCount(int booked_count) {
        return roomInventoryRepository.findByBookedCount(booked_count)
                .orElseThrow(() -> new IllegalStateException("Not found by this booked_count" + booked_count));
    }
}
