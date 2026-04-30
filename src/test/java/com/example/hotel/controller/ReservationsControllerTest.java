package com.example.hotel.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.example.hotel.model.*;
import com.example.hotel.service.ReservationService;
import tools.jackson.databind.ObjectMapper;

@WebMvcTest(ReservationsController.class)
@DisplayName("ReservationsController (API /api/v1)")
class ReservationsControllerTest {

    private static final LocalDate DISPONIBLE_DESDE = LocalDate.of(2026, 5, 1);
    private static final LocalDate DISPONIBLE_HASTA = LocalDate.of(2026, 5, 5);

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ReservationService reservationService;

    /** Cuerpo típico POST/PUT (sin id persistido). */
    private Reservation reservaBase;

    /** Habitación usada en pruebas de disponibilidad. */
    private Room habitacion101;

    @BeforeEach
    void setUp() {
        reset(reservationService);
        reservaBase = sampleReservation(null);
        habitacion101 = room(10L, "101");
    }

    @AfterEach
    void tearDown() {
        verifyNoMoreInteractions(reservationService);
    }

    @Test
    @DisplayName("GET /reservations: 200, JSON con lista del servicio y una sola llamada a getReservations()")
    void getReservations_responde200ConLista() throws Exception {
        Reservation r = sampleReservation(1L);
        when(reservationService.getReservations()).thenReturn(List.of(r));

        mockMvc.perform(get("/api/v1/reservations"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].guestEmail").value("huesped@example.com"));

        verify(reservationService).getReservations();
    }

    @Test
    @DisplayName("GET /reservations/{id}: 200 y cuerpo de la reserva; delega en getReservation(id)")
    void getReservation_porId_responde200() throws Exception {
        when(reservationService.getReservation(5L)).thenReturn(sampleReservation(5L));

        mockMvc.perform(get("/api/v1/reservations/5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(5));

        verify(reservationService).getReservation(5L);
    }

    @Test
    @DisplayName("GET /rooms_available/{inicio}/{fin}: parsea fechas ISO, 200 con habitaciones y delega en getRoomsAvailable")
    void getRoomsAvailable_responde200ConHabitaciones() throws Exception {
        when(reservationService.getRoomsAvailable(DISPONIBLE_DESDE, DISPONIBLE_HASTA))
                .thenReturn(List.of(habitacion101));

        mockMvc.perform(get("/api/v1/rooms_available/2026-05-01/2026-05-05"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].roomNumber").value("101"));

        verify(reservationService).getRoomsAvailable(DISPONIBLE_DESDE, DISPONIBLE_HASTA);
    }

    @Test
    @DisplayName("POST /reservation: 200 con JSON igual al resultado de createReservation() del servicio")
    void createReservation_responde200ConReservaGuardada() throws Exception {
        Reservation saved = sampleReservation(99L);
        when(reservationService.createReservation(any(Reservation.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/reservation")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaBase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(99));

        verify(reservationService).createReservation(any(Reservation.class));
    }

    @Test
    @DisplayName("PUT /reservation/{id}: 200 con reserva actualizada y delega en updateReservation(id, body)")
    void updateReservation_responde200ConReservaActualizada() throws Exception {
        Reservation updated = sampleReservation(7L);
        when(reservationService.updateReservation(eq(7L), any(Reservation.class)))
                .thenReturn(updated);

        mockMvc.perform(put("/api/v1/reservation/7")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reservaBase)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(7));

        verify(reservationService).updateReservation(eq(7L), any(Reservation.class));
    }

    @Test
    @DisplayName("DELETE /reservation/{id}: 200 vacío y delega en deleteReservation(id)")
    void deleteReservation_responde200() throws Exception {
        mockMvc.perform(delete("/api/v1/reservation/3"))
                .andExpect(status().isOk());

        verify(reservationService).deleteReservation(3L);
    }

    /** Genera una reserva de ejemplo para pruebas. */
    private static Reservation sampleReservation(Long id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 5));
        r.setGuestName("Ana");
        r.setGuestEmail("huesped@example.com");
        return r;
    }

    /** Genera una habitación de ejemplo para pruebas. */
    private static Room room(Long id, String roomNumber) {
        Room room = new Room();
        room.setId(id);
        room.setRoomNumber(roomNumber);
        return room;
    }
}
