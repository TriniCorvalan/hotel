package com.example.hotel.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;

@Service
public class ReservationServiceImpl implements ReservationService {
    @Autowired
    private ReservationRepository reservationRepository;
    @Autowired
    private RoomRepository roomRepository;
   
    @Override
    public List<Reservation> getReservations() {
        return reservationRepository.findAll();
    }
    
    @Override
    public Reservation getReservation(Long id) {
        return reservationRepository.findById(id).orElse(null);
    }
    
    /**
     * Habitaciones libres para un posible check-in/check-out.
     * Tanto la consulta como cada reserva usan el mismo criterio hotelero: noche ocupada en
     * {@code [startDate, endDate)} (check-in incluido, check-out excluido).
     */
    @Override
    public List<Room> getRoomsAvailable(LocalDate startDate, LocalDate endDate) {
        List<Room> availableRooms = new ArrayList<>();
        List<Reservation> reservations = getReservations();
        List<Room> rooms = roomRepository.findAll();
        for (Room room : rooms) {
            boolean occupied = false;
            for (Reservation reservation : reservations) {
                boolean reservedForThisRoom = false;
                for (Room r : reservation.getRooms()) {
                    if (r.getNumber().equals(room.getNumber())) {
                        reservedForThisRoom = true;
                        break;
                    }
                }
                if (!reservedForThisRoom) {
                    continue;
                }
                // Solapamiento de [reserva.inicio, reserva.fin) con [startDate, endDate):
                // reserva.inicio < endDate && reserva.fin > startDate
                if (reservation.getStartDate().isBefore(endDate)
                        && reservation.getEndDate().isAfter(startDate)) {
                    occupied = true;
                    break;
                }
            }
            if (!occupied) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        return reservationRepository.save(reservation);
    }
    
    @Override
    public Reservation updateReservation(Long id, Reservation reservation) {
        return reservationRepository.save(reservation);
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
