package com.example.hotel.service;

import java.util.List;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;

public interface ReservationService {
    List<Reservation> getReservations();
    Reservation getReservation(Long id);
    List<Room> getRoomsAvailable(String startDate, String endDate);
    Reservation createReservation(Reservation reservation);
    Reservation updateReservation(Long id, Reservation reservation);
    void deleteReservation(Long id);
}
