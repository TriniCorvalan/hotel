package com.example.hotel.model;

import java.time.LocalDate;
import java.util.List;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
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
    @NotNull(message = "La fecha de inicio es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    @Column(name = "start_date")
    private LocalDate startDate;

    /** Check-out (día de salida; la habitación queda libre ese día para un nuevo check-in). */
    @NotNull(message = "La fecha de fin es requerida")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
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

    public Reservation() {
    }
}
