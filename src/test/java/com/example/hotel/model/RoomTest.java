package com.example.hotel.model;
import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.Test;

public class RoomTest {
    @Test   
    void testGettersAndSetters() {
        Room room = new Room();
        room.setId(1L);
        room.setRoomNumber("101");
        room.setPrice(100);
        room.setCapacity(2);
        assertEquals(1L, room.getId());
        assertEquals("101", room.getRoomNumber());
        assertEquals(100, room.getPrice());
        assertEquals(2, room.getCapacity());
    }
}