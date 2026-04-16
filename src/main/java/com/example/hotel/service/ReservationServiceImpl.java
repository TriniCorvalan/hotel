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
        // 1) Leer las fechas que vienen en la petición (aún no se validan).
        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();

        // 2) Ambas fechas deben venir informadas; si no, no tiene sentido reservar.
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las fechas de inicio y fin son obligatorias");
        }

        // 3) El check-in debe ser antes del check-out (intervalo de al menos una noche).
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // 4) Debe pedirse al menos una habitación para esta reserva.
        if (reservation.getRooms() == null || reservation.getRooms().isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Debe indicar al menos una habitación");
        }

        // 5) Preparar datos para el recorrido: números ya vistos, lista final y reservas actuales.
        Set<String> seen = new LinkedHashSet<>();
        List<Room> assigned = new ArrayList<>();
        List<Reservation> existing = getReservations();

        // 6) Por cada habitación pedida: validar, buscar en BD y comprobar solapes con otras reservas.
        for (Room requested : reservation.getRooms()) {
            // 6a) La fila no puede ser nula ni venir sin número de habitación usable.
            if (requested == null || requested.getRoomNumber() == null || requested.getRoomNumber().isBlank()) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Cada habitación debe tener un número válido");
            }
            // 6b) Normalizar espacios en el número (ej. " 101 " → "101").
            String roomNumber = requested.getRoomNumber().trim();

            // 6c) Evitar que la misma petición reserve dos veces la misma habitación.
            if (!seen.add(roomNumber)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No se puede repetir la misma habitación en la misma reserva: " + roomNumber);
            }

            // 6d) Traer la entidad Room real desde la base (JPA necesita la instancia gestionada).
            Room managed = roomRepository.findByRoomNumber(roomNumber)
                    .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                            "No existe la habitación: " + roomNumber));

            // 6e) Si ya hay otra reserva que cruza esas fechas para esa habitación, rechazar (conflicto).
            if (isRoomOccupiedInPeriod(managed, start, end, existing)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La habitación no está disponible en esas fechas: " + roomNumber);
            }

            // 6f) Esta habitación pasó todas las pruebas; se agrega a la lista que se guardará.
            assigned.add(managed);
        }

        // 7) Sustituir los “stub” del JSON por las habitaciones correctas asociadas a la reserva.
        reservation.setRooms(assigned);

        // 8) Persistir y devolver la reserva ya con id generado por la base de datos.
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
