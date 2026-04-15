package com.example.hotel.service;

import java.time.LocalDate;
import java.util.List;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;

public interface ReservationService {
    List<Reservation> getReservations();
    Reservation getReservation(Long id);
    List<Room> getRoomsAvailable(LocalDate startDate, LocalDate endDate);
    Reservation createReservation(Reservation reservation);
    Reservation updateReservation(Long id, Reservation reservation);
    void deleteReservation(Long id);
}
