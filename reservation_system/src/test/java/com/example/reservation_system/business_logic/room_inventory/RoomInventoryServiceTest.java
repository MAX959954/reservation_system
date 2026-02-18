package com.example.reservation_system.business_logic.room_inventory;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RoomInventoryService")
class RoomInventoryServiceTest {
    @Mock 
    private RoomInventoryRepository roomInventoryRepository;

    @InjectMocks
    private RoomInventoryService roomInventoryService;

    private static final Long ROOM_ID = 1L;
    private static final LocalDate NIGHT  = LocalDate.of(2025 , 7 , 15);
    private static final LocalDate START = LocalDate.of(2025 , 7 , 10);
    private static final LocalDate END = LocalDate.of(2025 , 7 , 12);

    @Nested 
    @DisplayName("findByRoomIdAndNightDate")
    class findByRoomIdAndNightDate {
         

        @Test 
        @DisplayName("returns inventory when found")
        void returnsInventory () {
            RoomInventory roomInventory = new RoomInventory();
            when(roomInventoryRepository.findByRoomIdAndNightDate(ROOM_ID , NIGHT)).thenReturn(Optional.of(roomInventory));

            RoomInventory result = roomInventoryService.findByRoomIdAndNightDate(ROOM_ID, NIGHT);

            assertThat(result).isSameAs(roomInventory);
        }


        @Test 
        @DisplayName("throws when not found")
        void returnWhenNotFound() {
            when(roomInventoryRepository.findByRoomIdAndNightDate(ROOM_ID, NIGHT)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> roomInventoryService.findByRoomIdAndNightDate(ROOM_ID, NIGHT))
                .isInstanceOf(IllegalAccessException.class)
                .hasMessageContaining("Not found inventory");
        }
    }

    @Nested 
    @DisplayName("findByRoomIdAndNightDateBetween")
    class FindByRoomIdAndNightDateBetween {
        @Test
        @DisplayName("returns list of repository")
        void returnsFromRepository() {
            RoomInventory roomInventory = new RoomInventory(START , 0 , 3);
            when(roomInventoryRepository.findByRoomIdAndNightDateBetween(ROOM_ID, START , END))
                .thenReturn(List.of(roomInventory));

            List<RoomInventory> result = roomInventoryService.findByRoomIdAndNightDateBetween(ROOM_ID, START, END);

            assertThat(result).containsExactly(roomInventory);
        } 
    }

    @Nested 
    @DisplayName("findAvailableInventory")
    class findAvailableInventory {

        @Test 
        @DisplayName("returns list from repository")
        void returnsFromRepository() {
            RoomInventory roomInventory = new RoomInventory(START , 1 , 2);
            when(roomInventoryRepository.findAvailableInventory(START, END)).thenReturn(List.of());

            List<RoomInventory> result = roomInventoryService.findAvailableInventory(START, END);
            assertThat(result).containsExactly(roomInventory);
        }
    }
    
    @Nested 
    @DisplayName("save")
    class Save {

        @Test 
        @DisplayName("delegates to repository and returns saved entity")
        void delegatesToRepository() {
            RoomInventory roomInventory = new RoomInventory(NIGHT , 0 , 2);
            when(roomInventoryRepository.save(roomInventory)).thenReturn(roomInventory);

            RoomInventory result = roomInventoryService.save(roomInventory);

            assertThat(result).isSameAs(roomInventory);
            verify(roomInventoryRepository).save(roomInventory);
        }
    }
}