package com.example.reservation_system.business_logic.room_inventory;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import jakarta.persistence.LockModeType;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface RoomInventoryRepository extends JpaRepository<RoomInventory, Long> {
    
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.room.id = :roomId AND ri.nightDate BETWEEN :startDate AND :endDate")
    List<RoomInventory> findByRoomIdAndNightDateBetweenForUpdate(@Param("roomId") Long roomId, 
                                                               @Param("startDate") LocalDate startDate, 
                                                               @Param("endDate") LocalDate endDate);
    
    List<RoomInventory> findByRoomIdAndNightDateBetween(Long roomId, LocalDate startDate, LocalDate endDate);
    
    Optional<RoomInventory> findByRoomIdAndNightDate(Long roomId, LocalDate nightDate);
    
    @Query("SELECT ri FROM RoomInventory ri WHERE ri.nightDate BETWEEN :startDate AND :endDate AND ri.bookedCount < ri.allotment")
    List<RoomInventory> findAvailableInventory(@Param("startDate") LocalDate startDate, 
                                              @Param("endDate") LocalDate endDate);
}
