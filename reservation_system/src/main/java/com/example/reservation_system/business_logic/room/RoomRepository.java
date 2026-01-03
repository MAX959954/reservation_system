package com.example.reservation_system.business_logic.room;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoomRepository extends JpaRepository<Room , Long> {
    Optional<Room> findByName (String name);
    Optional<Room> findByNumber (String number);
    Optional<Room> findByType (String type);
    List<Room> findAllByType(String type);
}
