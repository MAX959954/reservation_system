package com.example.reservation_system.business_logic.rates;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("RatesService")

class RatesServiceTest {
    @Mock 
    private RatesRepository ratesRepository;

    @InjectMocks 
    private RatesService ratesService;

    private static final String ROOM_TYPE = "STANDARD";
    private static final LocalDate START_DATE = LocalDate.of(2025 , 6 , 1);

    @Nested 
    @DisplayName("findByRoomType")
    class FindByRoomType {

        @Test
        @DisplayName("returns rate when found")
        void returnsRateWhenFound() {
            Rates rate = new Rates(ROOM_TYPE , START_DATE , START_DATE.plusDays(1) , 120);
            when(ratesRepository.findByRoomType(ROOM_TYPE)).thenReturn(Optional.of(rate));

            Rates result = ratesService.findByRoomType(ROOM_TYPE);

            assertThat(result).isSameAs(rate);
            assertThat(result.getRoom_type()).isEqualTo(ROOM_TYPE);
            assertThat(result.getPrice()).isEqualTo(120);

        }

        @Test 
        @DisplayName("throws when not found")
        void throwsWhenNotFound() {
            when(ratesRepository.findByRoomType("UNKNOWN")).thenReturn(Optional.empty());
            assertThatThrownBy(() -> ratesService.findByRoomType("UNKNOWN"))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not found by this room type");
        }        
    }

    @Nested 
    @DisplayName("findByStartDate")
    class findByStartDate {
        @Test 
        @DisplayName("returns rate when found")
        void returnsWhenFound() {
            Rates rate = new Rates(ROOM_TYPE , START_DATE , START_DATE.plusMonths(1), 100);
            when(ratesRepository.findByStartDate(START_DATE)).thenReturn(Optional.of(rate));

            Rates result = ratesService.findByStartDate(START_DATE);

            assertThat(result).isSameAs(rate);
        }

        @Test 
        @DisplayName("throws when not found")
        void throwsWhenNotFound() {
            when(ratesRepository.findByStartDate(START_DATE)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> ratesService.findByStartDate(START_DATE))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not found by this start date");
        } 

    }
     
}