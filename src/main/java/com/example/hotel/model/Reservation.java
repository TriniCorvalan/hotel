package com.example.hotel.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Data
@Table(name = "reservations")
public class Reservation {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    /** Check-in (primer día ocupado). */
    @NotBlank(message = "La fecha de inicio es requerida")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Check-out (día de salida; la habitación queda libre ese día para un nuevo check-in). */
    @NotBlank(message = "La fecha de fin es requerida")
    @DateTimeFormat(pattern = "dd-MM-yyyy")
    @Column(name = "end_date")
    private LocalDate endDate;

    @Column(name = "guest_name")
    private String guestName;

    @NotBlank(message = "El email del huésped es requerido")
    @Pattern(regexp = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$", message = "El email no es válido")
    @Column(name = "guest_email")
    private String guestEmail;

    @ManyToMany
    @JoinTable(
            name = "reservation_rooms",
            joinColumns = @JoinColumn(name = "reservation_id"),
            inverseJoinColumns = @JoinColumn(name = "room_id"))
    private List<Room> rooms;

    public Reservation(Long id, LocalDate startDate, LocalDate endDate, String guestName,
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
