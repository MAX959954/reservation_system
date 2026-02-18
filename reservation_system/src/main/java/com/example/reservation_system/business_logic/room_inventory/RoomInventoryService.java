package com.example.reservation_system.business_logic.room_inventory;

import org.springframework.stereotype.Service;
import java.time.LocalDate;
import java.util.List;

@Service
public class RoomInventoryService {
    private final RoomInventoryRepository roomInventoryRepository;

    public RoomInventoryService(RoomInventoryRepository roomInventoryRepository) {
        this.roomInventoryRepository = roomInventoryRepository;
    }

    public RoomInventory findByRoomIdAndNightDate(Long roomId, LocalDate nightDate) {
        return roomInventoryRepository.findByRoomIdAndNightDate(roomId, nightDate)
                .orElseThrow(() -> new IllegalStateException("Not found inventory for room " + roomId + " on " + nightDate));
    }

    public List<RoomInventory> findByRoomIdAndNightDateBetween(Long roomId, LocalDate startDate, LocalDate endDate) {
        return roomInventoryRepository.findByRoomIdAndNightDateBetween(roomId, startDate, endDate);
    }

    public List<RoomInventory> findAvailableInventory(LocalDate startDate, LocalDate endDate) {
        return roomInventoryRepository.findAvailableInventory(startDate, endDate);
    }

    public RoomInventory save(RoomInventory roomInventory) {
        return roomInventoryRepository.save(roomInventory);
    }
}
