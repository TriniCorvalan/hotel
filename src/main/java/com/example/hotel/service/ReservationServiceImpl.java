package com.example.hotel.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

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
            if (!isRoomOccupiedInPeriod(room, startDate, endDate, reservations)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    /**
     * {@code true} si la habitación tiene al menos una noche en común con
     * {@code [startDate, endDate)} en alguna reserva existente.
     */
    private boolean isRoomOccupiedInPeriod(Room room, LocalDate startDate, LocalDate endDate,
            List<Reservation> reservations) {
        for (Reservation reservation : reservations) {
            if (reservation.getRooms() == null) {
                continue;
            }
            boolean reservedForThisRoom = false;
            for (Room r : reservation.getRooms()) {
                if (r.getRoomNumber().equals(room.getRoomNumber())) {
                    reservedForThisRoom = true;
                    break;
                }
            }
            if (!reservedForThisRoom) {
                continue;
            }
            if (reservation.getStartDate().isBefore(endDate)
                    && reservation.getEndDate().isAfter(startDate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public Reservation createReservation(Reservation reservation) {
        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las fechas de inicio y fin son obligatorias");
        }
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }
        if (reservation.getRooms() == null || reservation.getRooms().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar al menos una habitación");
        }

        Set<String> seen = new LinkedHashSet<>();
        List<Room> assigned = new ArrayList<>();
        List<Reservation> existing = getReservations();

        for (Room requested : reservation.getRooms()) {
            if (requested == null || requested.getRoomNumber() == null || requested.getRoomNumber().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada habitación debe tener un número válido");
            }
            String roomNumber = requested.getRoomNumber().trim();
            if (!seen.add(roomNumber)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede repetir la misma habitación en la misma reserva: " + roomNumber);
            }
            Room managed = roomRepository.findByRoomNumber(roomNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe la habitación: " + roomNumber));
            if (isRoomOccupiedInPeriod(managed, start, end, existing)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La habitación no está disponible en esas fechas: " + roomNumber);
            }
            assigned.add(managed);
        }

        reservation.setRooms(assigned);
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
