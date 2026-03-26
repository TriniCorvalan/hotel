package com.example.hotel;

import java.util.List;
import java.util.ArrayList;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ReservationsController {
    private List<Room> rooms = new ArrayList<>();
    private List<Reservation> reservations = new ArrayList<>();


    public ReservationsController() {
        // inicializar la lista de habitaciones
        rooms.add(new Room("101", "100", "1"));
        rooms.add(new Room("102", "200", "2"));
        rooms.add(new Room("103", "300", "3"));
        rooms.add(new Room("104", "400", "4"));
        rooms.add(new Room("105", "500", "5"));
        rooms.add(new Room("106", "600", "6"));
        rooms.add(new Room("107", "700", "7"));
        rooms.add(new Room("108", "800", "8"));
        rooms.add(new Room("109", "900", "9"));
        rooms.add(new Room("110", "1000", "10"));

        // inicializar la lista de reservas con sus habitaciones asignadas
        reservations.add(new Reservation("1", "2026-01-01", "2026-01-03", "John Doe",
                "john.doe@example.com", assignRooms("101", "102")));
        reservations.add(new Reservation("2", "2026-01-01", "2026-01-01", "Jane Doe",
                "jane.doe@example.com", assignRooms("102", "103")));
        reservations.add(new Reservation("3", "2026-01-07", "2026-01-09", "Jim Doe",
                "jim.doe@example.com", assignRooms("103", "104")));
        reservations.add(new Reservation("4", "2026-01-10", "2026-01-12", "Jill Doe",
                "jill.doe@example.com", assignRooms("104", "105")));
        reservations.add(new Reservation("5", "2026-01-13", "2026-01-15", "Jack Doe",
                "jack.doe@example.com", assignRooms("105", "106")));
        reservations.add(new Reservation("6", "2026-01-16", "2026-01-18", "Mary Doe",
                "mary.doe@example.com", assignRooms("106", "107")));
        reservations.add(new Reservation("7", "2026-01-19", "2026-01-21", "Adam Doe",
                "jack.doe@example.com", assignRooms("107", "108")));
        reservations.add(new Reservation("8", "2026-01-22", "2026-01-24", "Eve Doe",
                "eve.doe@example.com", assignRooms("108", "109")));
        reservations.add(new Reservation("9", "2026-01-25", "2026-01-27", "Charlie Doe",
                "charlie.doe@example.com", assignRooms("109", "110")));
        reservations.add(new Reservation("10", "2026-01-28", "2026-01-30", "Diana Doe",
                "diana.doe@example.com", assignRooms("110", "101")));
    }

    // Arma la lista de habitaciones para una reserva
    private List<Room> assignRooms(String... roomNumbers) {
        List<Room> assignedRooms = new ArrayList<>();
        for (String number : roomNumbers) {
            assignedRooms.add(roomByNumber(number));
        }
        return assignedRooms;
    }

    // Obtiene una habitación por su número
    private Room roomByNumber(String number) {
        for (Room r : rooms) {
            if (r.getNumber().equals(number)) {
                return r;
            }
        }
        return null;
    }

    // Obtiene todas las reservas
    @GetMapping("/reservations")
    public List<Reservation> getReservations() {
        return reservations;
    }

    // Obtiene una reserva por su id
    @GetMapping("/reservations/{id}")
    public Reservation getReservation(@PathVariable String id) {
        for (Reservation reservation : reservations) {
            if (reservation.getId().equals(id)) {
                return reservation;
            }
        }
        return null;
    }

    // Obtiene las habitaciones disponibles para una fecha de inicio y fin
    @GetMapping("/rooms_available/{startDate}/{endDate}")
    public List<Room> getRoomsAvailable(@PathVariable String startDate, @PathVariable String endDate) {
        List<Room> availableRooms = new ArrayList<>();
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
                // Solapamiento: [start_date, end_date] ∩ [reserva.inicio, reserva.fin] ≠ ∅
                if (reservation.getStartDate().compareTo(endDate) <= 0
                        && reservation.getEndDate().compareTo(startDate) >= 0) {
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
}
