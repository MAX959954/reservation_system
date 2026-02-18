package com.example.reservation_system.business_logic.search_room;

import com.example.reservation_system.business_logic.bookings.Booking;
import com.example.reservation_system.business_logic.bookings.BookingRepository;
import com.example.reservation_system.business_logic.booking_rooms.BookingRoomsRepository;
import com.example.reservation_system.business_logic.room.Room;
import com.example.reservation_system.business_logic.room.RoomRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SearchRoomService {
    private final RoomRepository roomRepository;
    private final BookingRepository bookingRepository;
    private final BookingRoomsRepository bookingRoomsRepository;

    public SearchRoomService(RoomRepository roomRepository, BookingRepository bookingRepository, BookingRoomsRepository bookingRoomsRepository) {
        this.roomRepository = roomRepository;
        this.bookingRepository = bookingRepository;
        this.bookingRoomsRepository = bookingRoomsRepository;
    }

    public List<Room> findAvailableRoom(LocalDate checkIn , LocalDate checkOut , String type){
        List<Room> allRooms = roomRepository.findAllByType(type);
        
        // Get bookings that overlap with the requested dates
        List<Booking> bookedBookings = bookingRepository.findRoomsBookedBetween(checkIn, checkOut);
        
        // Get room IDs from those bookings through BookingRooms
        List<Long> bookedRoomIds = bookingRoomsRepository.findByBookingIn(bookedBookings)
                .stream()
                .map(bookingRoom -> bookingRoom.getRoom().getId())
                .collect(Collectors.toList());

        return allRooms.stream()
                .filter(room -> !bookedRoomIds.contains(room.getId()))
                .collect(Collectors.toList());
    }
}
