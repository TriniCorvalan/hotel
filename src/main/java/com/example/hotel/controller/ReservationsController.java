package com.example.hotel.controller;

import java.time.LocalDate;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.hotel.model.*;
import com.example.hotel.service.ReservationService;
import org.springframework.web.bind.annotation.PutMapping;


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
    
    // Disponibilidad para check-in {@code startDate} y check-out {@code endDate} (fin exclusivo).
    @GetMapping("/rooms_available/{startDate}/{endDate}")
    public List<Room> getRoomsAvailable(@PathVariable String startDate, @PathVariable String endDate) {
        LocalDate start = LocalDate.parse(startDate);
        LocalDate end = LocalDate.parse(endDate);
        return reservationService.getRoomsAvailable(start, end);
    }

    // Crea una nueva reserva
    @PostMapping("/reservation")
    public Reservation createReservation(@RequestBody Reservation reservation) {
        logger.debug("Creating reservation: {}", reservation);
        return reservationService.createReservation(reservation);
    }

    // Actualiza una reserva existente
    @PutMapping("/reservation/{id}")
    public Reservation updateReservation(@PathVariable Long id, @RequestBody Reservation reservation) {
        return reservationService.updateReservation(id, reservation);
    }

    // Elimina una reserva existente
    @DeleteMapping("/reservation/{id}")
    public void deleteReservation(@PathVariable Long id) {
        reservationService.deleteReservation(id);
    }
}
