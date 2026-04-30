package com.example.hotel.model;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;

public class ReservationTest {
    @Test
    void testGettersAndSetters() {
        Reservation reservation = new Reservation();
        reservation.setId(1L);
        reservation.setStartDate(LocalDate.of(2026, 1, 1));
        reservation.setEndDate(LocalDate.of(2026, 1, 2));
        reservation.setGuestName("John Doe");
        reservation.setGuestEmail("john.doe@example.com");
        assertEquals(1L, reservation.getId());
        assertEquals(LocalDate.of(2026, 1, 1), reservation.getStartDate());
        assertEquals(LocalDate.of(2026, 1, 2), reservation.getEndDate());
        assertEquals("John Doe", reservation.getGuestName());
        assertEquals("john.doe@example.com", reservation.getGuestEmail());
    }
}