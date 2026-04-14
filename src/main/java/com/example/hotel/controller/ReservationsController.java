package com.example.hotel.controller;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;
import com.example.hotel.service.ReservationService;

@RestController
@RequestMapping("/api/v1")
@CrossOrigin(origins = "*")
public class ReservationsController {
    @Autowired
    private ReservationService reservationService;

    private final Logger logger = LoggerFactory.getLogger(ReservationsController.class);

    // Obtiene todas las reservas
    @GetMapping("/reservations")
    public List<Reservation> getReservations() {
        return reservationService.getReservations();
    }
    // Obtiene una reserva por su id
    @GetMapping("/reservations/{id}")
    public Reservation getReservation(@PathVariable Long id) {
        return reservationService.getReservation(id);
    }
    
    // Obtiene las habitaciones disponibles para una fecha de inicio y fin
    @GetMapping("/rooms_available/{startDate}/{endDate}")
    public List<Room> getRoomsAvailable(@PathVariable String startDate, @PathVariable String endDate) {
        return reservationService.getRoomsAvailable(startDate, endDate);
    }

    // Crea una nueva reserva
    @PostMapping("/reservation/{id}")
    public Reservation createReservation(@RequestBody Reservation reservation) {
        logger.debug("Creating reservation: {}", reservation);
        return reservationService.createReservation(reservation);
    }
}
