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
            if (!isRoomOccupiedInPeriod(room, startDate, endDate, reservations, null)) {
                availableRooms.add(room);
            }
        }
        return availableRooms;
    }

    /**
     * {@code true} si la habitación tiene al menos una noche en común con
     * {@code [startDate, endDate)} en alguna reserva existente.
     *
     * @param excludeReservationId si no es {@code null}, se ignora esa reserva (PUT: no chocar
     *        con la propia fila que se edita).
     */
    private boolean isRoomOccupiedInPeriod(Room room, LocalDate startDate, LocalDate endDate,
            List<Reservation> reservations, Long excludeReservationId) {
        for (Reservation reservation : reservations) {
            if (excludeReservationId != null && excludeReservationId.equals(reservation.getId())) {
                continue;
            }
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

        // 5–6) Sustituir stubs del JSON por entidades {@link Room} gestionadas y validar solapes.
        reservation.setRooms(validateAndResolveRooms(reservation, start, end, null));

        // 8) Persistir y devolver la reserva ya con id generado por la base de datos.
        return reservationRepository.save(reservation);
    }
    
    /**
     * Actualiza una reserva existente identificada por {@code id} en la URL.
     * Primero busca esa fila en base de datos; si no existe, error 404.
     * Lee fechas y datos de huésped desde el cuerpo de la petición (no crea una entidad nueva desde cero).
     * Valida que inicio y fin vengan informados y que el inicio sea anterior al fin.
     * Copia fechas, nombre y correo del cuerpo sobre la entidad ya cargada (la que JPA conoce por id).
     * Resuelve habitaciones con la misma lógica que en alta: números válidos, sin duplicados en el body, existencia en BD.
     * Al comprobar solapes ignora la propia reserva {@code id}, para no tratarse como conflicto consigo misma.
     * Guarda la entidad {@code existing} actualizada y la devuelve.
     *
     * @param id identificador de la reserva a modificar (coincide con la ruta {@code PUT .../reservation/{id}}).
     * @param reservation cuerpo con los nuevos valores (fechas, huésped, lista de habitaciones).
     * @return la reserva persistida tras el cambio.
     * @throws ResponseStatusException 404 si no hay reserva con ese id.
     * @throws ResponseStatusException 400 si faltan fechas, el orden es inválido o la lista de habitaciones no cumple reglas.
     * @throws ResponseStatusException 404 si algún número de habitación del cuerpo no existe.
     * @throws ResponseStatusException 409 si alguna habitación quedaría ocupada por otra reserva en esas fechas.
     */
    @Override
    public Reservation updateReservation(Long id, Reservation reservation) {
        // 1) Localizar la reserva que realmente vamos a mutar; el id viene de la URL.
        Reservation existing = reservationRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Reserva no encontrada: " + id));

        // 2) Tomar las nuevas fechas del JSON (aún sin aplicar al objeto persistido).
        LocalDate start = reservation.getStartDate();
        LocalDate end = reservation.getEndDate();

        // 3) Mismas reglas que en creación: ambas fechas obligatorias.
        if (start == null || end == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Las fechas de inicio y fin son obligatorias");
        }

        // 4) El intervalo debe ser coherente (al menos una noche).
        if (!start.isBefore(end)) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "La fecha de inicio debe ser anterior a la fecha de fin");
        }

        // 5) Volcar datos editables sobre la entidad gestionada (mantiene el mismo id y metadatos de JPA).
        existing.setStartDate(start);
        existing.setEndDate(end);
        existing.setGuestName(reservation.getGuestName());
        existing.setGuestEmail(reservation.getGuestEmail());

        // 6) Resolver habitaciones y disponibilidad; se excluye esta reserva al buscar choques con otras filas.
        existing.setRooms(validateAndResolveRooms(reservation, start, end, id));

        // 7) Persistir los cambios sobre la fila existente y devolver el estado guardado.
        return reservationRepository.save(existing);
    }

    /**
     * Convierte cada habitación del cuerpo (solo número u otros campos parciales) en filas
     * {@link Room} persistidas y comprueba disponibilidad. {@code excludeReservationId}
     */
    private List<Room> validateAndResolveRooms(Reservation reservation, LocalDate start, LocalDate end,
            Long excludeReservationId) {
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

            if (isRoomOccupiedInPeriod(managed, start, end, existing, excludeReservationId)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "La habitación no está disponible en esas fechas: " + roomNumber);
            }
            assigned.add(managed);
        }
        return assigned;
    }

    @Override
    public void deleteReservation(Long id) {
        reservationRepository.deleteById(id);
    }
}
