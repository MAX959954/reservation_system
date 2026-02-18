package com.example.reservation_system.business_logic.bookings;

import com.example.reservation_system.business_logic.booking_rooms.BookingRoomsRepository;
import com.example.reservation_system.business_logic.room_inventory.RoomInventory;
import com.example.reservation_system.business_logic.room_inventory.RoomInventoryRepository;
import com.example.reservation_system.model.AppUser;
import com.example.reservation_system.model.AppUserRepository;
import com.example.reservation_system.model.AppUserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class) 
@DisplayName("BookingService")
public class BookingServiceTest {
    @Mock 
    private BookingRepository bookingRepository; 
    @Mock 
    private BookingRoomsRepository bookingRoomsRepository;
    @Mock 
    private RoomInventoryRepository roomInventoryRepository; 
    @Mock 
    private AppUserRepository appUserRepository ;

    @InjectMocks
    private BookingService bookingService;

    private static final Long USER_ID = 1L;
    private  static final LocalDate CHECK_IN = LocalDate.now().plusDays(2);
    private static final LocalDate CHECK_OUT = LocalDate.now().plusDays(4);
    private static final BigDecimal TOTAL_AMOUNT = new BigDecimal("299.00");

    //@Nested is a JUnit 5 annotation that allows you to:
    //Create an inner test class inside another test class
    @Nested
    @DisplayName("createBooking")
    class CreateBooking {
        @Test 
        @DisplayName("thrwos when check-in is after check-out")
        void throwsWhenCheckInAfterCheckOut() {
            /*
            Booking cmd allows bookings like:
            Room 1 → 2 adults, 0 children
            Room 2 → 1 adult, 1 child
            Room 3 → 2 adults, 2 children
            */
            CreateBookingCmd cmd = new CreateBookingCmd (
                CHECK_OUT , CHECK_IN , 
                // The L means: "this number is a long, not an int"
                /*So your service likely supports:
                Multiple rooms
                Multiple adult counts
                Multiple child counts*/

                //room_id = 1     2 adults       0 children  - it just mimimal valid booking setup.
                List.of(1L) , List.of(2), List.of(0) , 
                TOTAL_AMOUNT ,  USER_ID
            );

            assertThatThrownBy(() -> bookingService.createBooking(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Check-in date must be before check-out date");
        }

        @Test 
        @DisplayName("throws when room count does not match adults/children count")
        void throwWhenRoomCountMismatch () {
            CreateBookingCmd cmd = new CreateBookingCmd(
                CHECK_IN , CHECK_OUT , 
                List.of(1L , 2L) , List.of(2) , List.of(0 , 1 ),  // 2 rooms but only 1 adult entry
                TOTAL_AMOUNT ,  USER_ID 
            ) ; 
            
            assertThatThrownBy( () -> bookingService.createBooking(cmd))
                .isInstanceOf(IllegalArgumentException.class) 
                .hasMessageContaining("Number of rooms must match");
        }

        @Test 
        @DisplayName("throws when user not found")
        void throwWhenUserNotFound () {
            CreateBookingCmd cmd = new CreateBookingCmd(
                CHECK_IN , CHECK_OUT , 
                List.of(1L) , List.of(2) , List.of(0),
                                                //is just a fake ID
                TOTAL_AMOUNT , 999L 
            ); 
            when(appUserRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy ( () -> bookingService.createBooking(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("User not found"); 
        }

        @Test 
        @DisplayName("throws when room has no availability for night") 
        void throwsWhenRoomNotAvailable () {
            CreateBookingCmd cmd = new CreateBookingCmd(
                CHECK_IN , CHECK_OUT , 
                List.of(1L) , List.of(2) , List.of(0) ,
                TOTAL_AMOUNT ,  USER_ID  
            ); 

            AppUser appUser = createAppUser();
            when(appUserRepository.findById(USER_ID).thenReturn(Optional.of(appUser)));

            RoomInventory fullInventory = new RoomInventory();
            fullInventory.setNight_date(CHECK_IN);
            fullInventory.setBooked_count(5);
            fullInventory.setAllotment(5);
            when(roomInventoryRepository.findByRoomIdAndNightDateBetweenForUpdate(1L, CHECK_IN, CHECK_OUT))
                .thenReturn(List.of(fullInventory));

            assertThatThrownBy (() -> bookingService.createBooking(cmd))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("room is not available");

        }

        @Test 
        @DisplayName("thows when creates booking and updates inventory when valid")
        void createBookingAndUpdatesInventory() {
            CreateBookingCmd cmd = new CreateBookingCmd(
                CHECK_IN , CHECK_OUT , 
                List.of(1L) , List.of(2) , List.of(0), 
                TOTAL_AMOUNT ,  USER_ID 
            ); 

            AppUser user =  createAppUser();
            when(appUserRepository.findById(USER_ID)).thenReturn(Optional.of(user));

            RoomInventory available = new RoomInventory();
            available.setBooked_count(0);
            available.setAllotment(3);
            available.setNight_date(CHECK_IN);

            when(roomInventoryRepository.findByRoomIdAndNightDateBetween(1L, CHECK_IN, CHECK_OUT)).thenReturn(List.of(available));

            Booking saved = new Booking();
            saved.setId(10L); 
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv ->{
                Booking b = inv.getArgument(0);
                if (b.getId() == null) b.setId(10L);
                return b;
            }); 

            Booking result = bookingService.createBooking(cmd);

            assertThat(result).isNotNull();
            assertThat(result.getStatus()).isEqualTo(BookingStatus.PENDING_PAYMENT);
            assertThat(result.getCheck_in()).isEqualTo(CHECK_IN);
            assertThat(result.getCheck_out()).isEqualTo(CHECK_OUT);
            assertThat(result.getTotal_amount()).isEqualTo(TOTAL_AMOUNT);
            assertThat(result.getAppUser()).isEqualTo(user);

            verify(roomInventoryRepository).save(available);
            assertThat(available.getBooked_count()).isEqual(1);
        }
    }

    @Nested 
    @DisplayName("findById")
    class FindById {
        @Test
        @DisplayName("returns booking when found")
        void returnBookingWhenFound () {
            Booking booking = new Booking();
            booking.setId(1L);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(booking));

            assertThat(bookingService.findById(1L)).isSameAs(booking);
        }

        @Test 
        @DisplayName("throws when not found")
        void returnWhenNotFound() {
           when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

           assertThatThrownBy(() -> bookingService.findById(999L))
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("No booking for that id");
        }
    }

    @Nested 
    @DisplayName("findBookingForUser")
    class findBookingForUser {
        @Test 
        @DisplayName("filters booking by used id")
        void filtersBookingByUserId () {
            Booking b1 = new Booking();
            b1.setId(1L);
            b1.setAppUser(createAppUser(USER_ID));
            Booking b2 = new Booking();
            b2.setId(2L);
            AppUser other = createAppUser(2L);
            other.setId(2L);
            b2.setAppUser(other);
            when(bookingRepository.findBookings()).thenReturn(List.of(b1 , b2));

            List<Booking> result = bookingRepository.findBookingForUser(USER_ID);
            assertThat(result).contains(b1);
        }
    }


    @Nested 
    @DisplayName("updateBooking")
    class updateBooking {
        @Test 
        @DisplayName("updates check-in and check-out")
        void updatesDates () {
            Booking existing  = new Booking();
            existing.setId(1L);
            existing.setCheck_in(CHECK_IN);
            existing.setCheck_out(CHECK_OUT);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArguments(0));

            LocalDate newIn = CHECK_IN.plusDays(1);
            LocalDate newOut = CHECK_OUT.plusDays(1);
            Booking result = bookingService.updateBooking(1L, newIn, newIn);

            assertThat(result.getCheck_in()).isEqualTo(newIn);
            assertThat(result.getCheck_out()).isEqualTo(newOut);
            verify(bookingRepository).save(existing);
        }

        @Test 
        @DisplayName("thorws when booking not found")
        void throwsWhenNotFound() {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateBooking(999L , CHECK_IN , CHECK_OUT))
             .isInstanceOf(IllegalStateException.class)
             .hasMessageContaining("Not found");
        }
    }

    @Nested 
    @DisplayName("updateBookingService")
    class updateBookingService {
        @Test 
        @DisplayName("updates status and returns saved booking")
        void updateStatus () {
            Booking existing = new Booking();
            existing.setId(1L);
            existing.setStatus(BookingStatus.PENDING_PAYMENT);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            Booking result = bookingService.updateBookingStatus(1L,  "CONFIRMED");
            assertThat(result.getStatus()).isEqualTo(BookingStatus.CONFIRMED);
            verify(bookingRepository).save(existing);
        }

        @Test 
        @DisplayName ("throws when booking not found") 
        void thrwosWheNotFound () {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> bookingService.updateBookingStatus(999L , "CONFIRMED"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking not found");
        }
    }

    @Nested 
    @DisplayName("cancelBooking")
    class CancelBooking {
        @Test 
        @DisplayName("sets status to CANCELLED")
        void setStatusCancelled() {
            Booking existing = new Booking();
            existing.setId(1L);
            existing.setStatus(BookingStatus.CONFIRMED);
            when(bookingRepository.findById(1L)).thenReturn(Optional.of(existing));
            when(bookingRepository.save(any(Booking.class))).thenAnswer(inv -> inv.getArgument(0));

            Booking result = bookingService.cancelBooking(1L);

            assertThat(result.getStatus()).isEqualTo(BookingStatus.CANCELLED);
            verify(bookingRepository).save(existing);
        }


        @Test 
        @DisplayName("throws when booking not found")
        void throwsWhenBookingNotFound () {
            when(bookingRepository.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->bookingService.cancelBooking(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not found");
        }
    }

    @Nested 
    @DisplayName("deleteBooking")
    class DeleteBooking {

        @Test 
        @DisplayName("delete when exists")
        void deletesWhenExists () {
            when(bookingRepository.existsById(1L)).thenReturn(true);
            bookingService.deleteBooking(1L);
            verify(bookingRepository).deleteById(1L);
        }

        @Test 
        @DisplayName("throws when booking does not exist")
        void throwsWhenBookingNotExists () {
            when(bookingRepository.existsById(999L)).thenReturn(false);

            assertThatThrownBy(() -> bookingService.deleteBooking(999L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Booking not found");
        }
    }

    @Nested 
    @DisplayName("getTotalBookings")
    class getTotalBookings {
        @Test 
        @DisplayName("returns count from repository")
        void returnsCount() {
            when(bookingRepository.getTotalBookings()).thenReturn(42L);
            assertThat(bookingService.getTotalBookings()).isEqualTo(42L);
        } 
    }

    private static AppUser createAppUser() {
        return createAppUser(USER_ID);
    }

    private static AppUser createAppUser (Long id ) {
        AppUser user = new AppUser("user" , "Full Name" , "user@example.com" , "encoded" , LocalDate.now() , AppUserRole.GUEST);
        user.setId(id);
        return user;
    }

}
