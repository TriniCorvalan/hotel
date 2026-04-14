package com.example.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hotel.model.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    
}
