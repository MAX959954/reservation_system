package com.example.reservation_system.business_logic.bookings;

import org.springframework.stereotype.Service;

import com.example.reservation_system.model.AppUserRepository;
import com.example.reservation_system.business_logic.booking_rooms.BookingRooms;
import com.example.reservation_system.business_logic.booking_rooms.BookingRoomsRepository;
import com.example.reservation_system.business_logic.room_inventory.RoomInventory;
import com.example.reservation_system.business_logic.room_inventory.RoomInventoryRepository;
import com.example.reservation_system.model.AppUser;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class BookingService {
    private final BookingRepository bookingRepository;
    private final BookingRoomsRepository bookingRoomsRepository;
    private final RoomInventoryRepository roomInventoryRepository;
    private final AppUserRepository appUserRepository;

    public BookingService(BookingRepository bookingRepository,
                         BookingRoomsRepository bookingRoomsRepository,
                         RoomInventoryRepository roomInventoryRepository,
                         AppUserRepository appUserRepository) {
        this.bookingRepository = bookingRepository;
        this.bookingRoomsRepository = bookingRoomsRepository ; 
        this.roomInventoryRepository = roomInventoryRepository;
        this.appUserRepository = appUserRepository;
    }

    @Transactional(readOnly = true)
    public Booking findById(Long id){
        return bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("No booking for that id"));
    }

    @Transactional(readOnly = true)
    public Booking findByDateCheckIn (LocalDate check_in) {
        return bookingRepository.findByDateCheckIn(check_in)
                .orElseThrow(() -> new IllegalStateException("No booking found for check-in" + check_in));
    }

    @Transactional(readOnly = true)
    public Booking findByCreatedAt(LocalDate created_at) {
        return bookingRepository.findByCreatedAt(created_at)
                .orElseThrow(() -> new IllegalStateException("No booking found for this " + created_at ));
    }

    @Transactional
    public Booking createBooking(CreateBookingCmd cmd) {
        if (cmd.getCheckIn().isAfter(cmd.getCheckOut())){
            throw new IllegalArgumentException("Check-in date must be before check-out date");
        }

        if  (cmd.getRoomIds().size() != cmd.getAdults().size() || cmd.getRoomIds().size() != cmd.getChildren().size()){
            throw new IllegalArgumentException("Number of rooms must match number of adults and children");
        }

        AppUser user = appUserRepository.findById(cmd.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found by this id" + cmd.getUserId()));

        //lock inventory rows for all rooms and dates 
        List<RoomInventory> lockedInventories = new ArrayList<>();
        
        for (Long roomId : cmd.getRoomIds()){
            List<RoomInventory> roomInventories = roomInventoryRepository.findByRoomIdAndNightDateBetween(roomId, cmd.getCheckIn(), cmd.getCheckOut());
        
            //check availability for each night 
            for (RoomInventory inventory : roomInventories){
                if (inventory.getBooked_count() >= inventory.getAllotment()){
                    throw new IllegalArgumentException("Room " + roomId + " is not available on " + inventory.getNight_date() + 
                        ". Booked: " + inventory.getBooked_count() + "/" + inventory.getAllotment());
                }
            }

            lockedInventories.addAll(roomInventories);
        }

        //create booking with Pending_payment status 
        Booking booking = new Booking();
        booking.setCheck_in(cmd.getCheckIn());
        booking.setCheck_out(cmd.getCheckOut());
        booking.setTotal_amount(cmd.getTotalAmount());
        booking.setCurrency(cmd.getCurrency());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreated_at(LocalDate.now());
        booking.setUpdated_at(LocalDate.now());
        booking.setAppUser(user);
        bookingRepository.save(booking);

        booking = bookingRepository.save(booking);

        //creeate booking_rooms records 
        for(int i = 0 ; i < cmd.getRoomIds().size() ; i++) {
            BookingRooms bookingRoom = new BookingRooms (
                booking ,
                null , 
                cmd.getAdults().get(i), 
                cmd.getChildren().get(i), 
                LocalDate.now()

            );
            bookingRoomsRepository.save(bookingRoom);
        }

        //update inventory booked counts 

        for(RoomInventory inventory : lockedInventories){
            inventory.setBooked_count(inventory.getBooked_count() + 1);
            roomInventoryRepository.save(inventory);
        }

        return booking;
    }

    public Booking CreateBooking (Booking booking) {
        booking.setCreated_at(LocalDate.now());
        booking.setUpdated_at(LocalDate.now());
        booking.setStatus(BookingStatus.RESERVED);
        return bookingRepository.save(booking);
    }

    @Transactional
    public Booking updateBookingStatus (Long id , String status) {
        Booking existing = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Booking not found by this id" + id));

        existing.setStatus(BookingStatus.valueOf(status));
        existing.setUpdated_at(LocalDate.now());
        return bookingRepository.save(existing);
    }

    @Transactional
    public Booking updateBooking(Long bookingId ,LocalDate newCheckIn , LocalDate newCheckedOut ) {
        Booking existingBooking =  bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Not found by this id" + bookingId));

        existingBooking.setCheck_in(newCheckIn);
        existingBooking.setCheck_out(newCheckedOut);
        existingBooking.setUpdated_at(LocalDate.now());

        return bookingRepository.save(existingBooking);
    }

    @Transactional 
    public void deleteBooking(Long id) {
        if (!bookingRepository.existsById(id)){
            throw new IllegalStateException("Booking not found by this Id" + id);
        }
        bookingRepository.deleteById(id);
    }

    @Transactional 
    public Booking cancelBooking(Long bookingId) {
        Booking existingBooking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalStateException("Not found by this id" + bookingId));

        existingBooking.setStatus(BookingStatus.CANCELLED);
        existingBooking.setUpdated_at(LocalDate.now());
        return bookingRepository.save(existingBooking);
    }

    @Transactional(readOnly = true)
    public List<Booking> findRoomsBookedBetween(LocalDate check_in, LocalDate check_out) {
        List<Booking> allBookings = bookingRepository.findAll();

        return allBookings.stream()
                .filter(booking ->
                        booking.getCheck_in().isBefore(check_out) &&
                                booking.getCheck_out().isAfter(check_in)
                )
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public long getTotalBookings() {
        long totalBookings = bookingRepository.getTotalBookings();
        return totalBookings;
    }

    @Transactional(readOnly = true)
    public List<Booking> findByUserId(Long userId) {
        return bookingRepository.findByUserId(userId);
    }

}
