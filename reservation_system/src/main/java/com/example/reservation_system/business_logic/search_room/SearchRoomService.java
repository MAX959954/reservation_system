package com.example.reservation_system.business_logic.search_room;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.room.Room;
import com.example.reservation_system.business_logic.room.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchRoomService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;

    public SearchRoomService(RoomRepository roomRepository, BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Room> findAvailableRoom(LocalDateTime checkIn , LocalDateTime checkOut , String type){
        List<Room> allRooms = roomRepository.findAllByType(type);
        List<Room> bookedRooms = bookingRepository.findRoomsBookedBetween(checkIn , checkOut)
                .stream()
                .map(Booking::getRoom)
                .toList();

        return allRooms.stream()
                .filter(room ->!bookedRooms.contains(room))
                .collect(Collectors.toList());
    }
}
