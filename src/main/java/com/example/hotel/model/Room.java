package com.example.hotel.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "rooms")
public class Room {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Número de habitación (columna {@code room_number}: {@code NUMBER} es palabra reservada en Oracle). */
    @Column(name = "room_number")
    private String roomNumber;
    private int price;
    private int capacity;

    public Room() {
    }
}
