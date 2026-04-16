package com.example.hotel.config;

import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.DependsOn;
import org.springframework.context.event.EventListener;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.example.hotel.model.Room;
import com.example.hotel.repository.RoomRepository;

/**
 * Carga datos iniciales de habitaciones al arrancar la aplicación.
 * <p>
 * Usa {@link ApplicationReadyEvent} en lugar de {@link org.springframework.boot.ApplicationRunner}
 * para ejecutarse cuando el contexto está listo y JPA/Hibernate ya ha aplicado el esquema
 * ({@code ddl-auto}), evitando ORA-00942 al consultar tablas aún inexistentes (p. ej. con Oracle
 * o reinicios de DevTools).
 * <p>
 * Si la tabla {@code rooms} ya contiene filas, no inserta nada para evitar duplicados
 * (útil si se cambia {@code spring.jpa.hibernate.ddl-auto} de {@code create} a
 * {@code update} y se conservan datos entre reinicios).
 */
@Component
@DependsOn("entityManagerFactory")
public class RoomDataLoader {

    private final RoomRepository roomRepository;

    public RoomDataLoader(RoomRepository roomRepository) {
        this.roomRepository = roomRepository;
    }

    /**
     * Crea 10 habitaciones por defecto (números {@code 101}–{@code 110}) solo si el
     * repositorio está vacío. Precio base 125.000–170.000 según habitación; capacidad 2
     * en las cinco primeras y 4 en las cinco restantes.
     */
    @Order(Ordered.LOWEST_PRECEDENCE)
    @Transactional
    @EventListener(ApplicationReadyEvent.class)
    public void seedRooms() {
        if (roomRepository.count() > 0) {
            return;
        }
        for (int i = 1; i <= 10; i++) {
            String roomNumber = String.valueOf(100 + i);
            int price = 120_000 + i * 5_000;
            int capacity = i <= 5 ? 2 : 4;
            Room room = new Room();
            room.setRoomNumber(roomNumber);
            room.setPrice(price);
            room.setCapacity(capacity);
            roomRepository.save(room);
        }
    }
}
