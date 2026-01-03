package com.example.reservation_system.business_logic.room;

import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomService {

    private final RoomRepository roomRepository;

    public RoomService(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    public Room createRoom (Room room) {
        return roomRepository.save(room);
    }

    public Room updateRoom (Long Id ,Room updatedRoom ){
        Room existing = roomRepository.findById(Id)
                .orElseThrow(()-> new  IllegalStateException("Room doesn't exist: " + Id));

        existing.setNumber(updatedRoom.getNumber());
        existing.setName(updatedRoom.getName());
        existing.setCapacity(updatedRoom.getCapacity());
        existing.setStatus(updatedRoom.getStatus());
        existing.setBase_price(updatedRoom.getBase_price());
        return roomRepository.save(existing);
    };

    public void deleteRoom(Long Id) {
        if (!roomRepository.existsById(Id)){
            throw new IllegalStateException("Room not found by this Id" + Id);
        }
        roomRepository.deleteById(Id);
    }

    public Room findByName (String name) {
        return roomRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Not found by this name " + name ));
    }

    public Room findByNumber(String number) {
        return roomRepository.findByNumber(number)
                .orElseThrow(() -> new IllegalStateException("Not found by this number " + number));
    }

    public Room findByType (String type) {
        return roomRepository.findByType(type)
                .orElseThrow(() ->new IllegalStateException("Not found by this type"));
    }

    public List<Room> findAllByType(String type){
        List<Room> allRooms = roomRepository.findAll();

        return allRooms.stream()
                .filter(room ->room.getType().equalsIgnoreCase(type))
                .collect(Collectors.toList());
    }


}
