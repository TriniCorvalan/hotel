package com.example.hotel.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.hotel.model.Room;

public interface RoomRepository extends JpaRepository<Room, Long> {
    
}
