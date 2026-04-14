package com.example.hotel.model;

import java.util.List;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Data
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;
    private String startDate;
    private String endDate;
    private String guestName;
    private String guestEmail;

    @ManyToMany
    @JoinTable(
            name = "reservation_rooms",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id"))
    private List<Room> rooms;

    public Reservation(Long id, String startDate, String endDate, String guestName,
            String guestEmail, List<Room> rooms) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.rooms = rooms;
    }

    public Reservation() {
    }
}
