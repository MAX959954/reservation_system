package com.example.reservation_system.business_logic.room_inventory;

import com.example.reservation_system.business_logic.room.Room;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

@Entity
@Table(name = "room_inventory")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class RoomInventory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate night_date;

    private int booked_count;

    private int allotment;

    @ManyToOne
    @JoinColumn(
            name = "room_id",
            nullable = false,
            referencedColumnName = "id"
    )
    private Room room;

    public RoomInventory(LocalDate night_date, int booked_count, int allotment) {
        this.night_date = LocalDate.from(night_date);
        this.booked_count = booked_count;
        this.allotment = allotment;
    }
}
