package com.example.hotel.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import com.example.hotel.model.Reservation;
import com.example.hotel.model.Room;
import com.example.hotel.repository.ReservationRepository;
import com.example.hotel.repository.RoomRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceImplTest {

    private static final long ID_RESERVA = 1L;
    private static final String NUMERO_HABITACION = "101";

    @Mock
    private ReservationRepository repository;

    @Mock
    private RoomRepository roomRepository;

    @InjectMocks
    private ReservationServiceImpl service;

    /** Cuerpo típico (sin id): fechas, huésped y una habitación pedida en el JSON. */
    private Reservation reservaBase;

    /** Misma reserva persistida con {@link #ID_RESERVA} y habitación ya gestionada por JPA. */
    private Reservation reservaPersistidaId1;

    /** Habitación persistida devuelta por {@link RoomRepository#findByRoomNumber(String)}. */
    private Room habitacion101Gestionada;

    /** Habitación “del request” (solo número), reutilizable en alta y edición. */
    private Room habitacion101Solicitud;

    @BeforeEach
    void setUp() {
        reset(repository);
        reset(roomRepository);

        habitacion101Gestionada = managedRoom(10L, NUMERO_HABITACION);
        habitacion101Solicitud = roomWithNumber(NUMERO_HABITACION);

        reservaBase = sampleReservation(null);
        reservaBase.setRooms(List.of(habitacion101Solicitud));

        reservaPersistidaId1 = sampleReservation(ID_RESERVA);
        reservaPersistidaId1.setRooms(List.of(habitacion101Gestionada));
    }

    @Test
    void testGetReservations() {
        List<Reservation> expected = List.of(reservaBase);
        when(repository.findAll()).thenReturn(expected);
        assertEquals(expected, service.getReservations());
    }

    @Test
    void testGetReservation() {
        when(repository.findById(ID_RESERVA)).thenReturn(Optional.of(reservaBase));
        assertEquals(reservaBase, service.getReservation(ID_RESERVA));
    }

    @Test
    void testCreateReservation() {
        when(repository.findAll()).thenReturn(List.of());
        when(roomRepository.findByRoomNumber(NUMERO_HABITACION)).thenReturn(Optional.of(habitacion101Gestionada));
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = service.createReservation(reservaBase);

        assertEquals(NUMERO_HABITACION, result.getRooms().getFirst().getRoomNumber());
        verify(repository).save(any(Reservation.class));
    }

    @Test
    void updateReservationExists() {
        when(repository.findById(ID_RESERVA)).thenReturn(Optional.of(reservaPersistidaId1));
        when(repository.findAll()).thenReturn(List.of(reservaPersistidaId1));
        when(roomRepository.findByRoomNumber(NUMERO_HABITACION)).thenReturn(Optional.of(habitacion101Gestionada));
        when(repository.save(any(Reservation.class))).thenAnswer(inv -> inv.getArgument(0));

        Reservation result = service.updateReservation(ID_RESERVA, reservaBase);

        assertEquals(ID_RESERVA, result.getId());
        assertEquals(reservaBase.getGuestEmail(), result.getGuestEmail());
        verify(repository).save(reservaPersistidaId1);
    }

    @Test
    void updateReservationNotExists() {
        when(repository.findById(ID_RESERVA)).thenReturn(Optional.empty());
        assertThrows(ResponseStatusException.class, () -> service.updateReservation(ID_RESERVA, reservaBase));
        verify(repository, never()).save(any());
    }

    @Test
    void deleteReservation() {
        service.deleteReservation(ID_RESERVA);
        verify(repository).deleteById(ID_RESERVA);
    }

    private static Reservation sampleReservation(Long id) {
        Reservation r = new Reservation();
        r.setId(id);
        r.setStartDate(LocalDate.of(2026, 6, 1));
        r.setEndDate(LocalDate.of(2026, 6, 5));
        r.setGuestName("Ana");
        r.setGuestEmail("huesped@example.com");
        return r;
    }

    private static Room roomWithNumber(String roomNumber) {
        Room room = new Room();
        room.setRoomNumber(roomNumber);
        return room;
    }

    private static Room managedRoom(long id, String roomNumber) {
        Room room = new Room();
        room.setId(id);
        room.setRoomNumber(roomNumber);
        room.setPrice(100);
        room.setCapacity(2);
        return room;
    }
}
