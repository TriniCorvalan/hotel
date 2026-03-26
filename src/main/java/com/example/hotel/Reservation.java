package com.example.hotel;

import java.util.List;

public class Reservation {
    private String id;
    private String startDate;
    private String endDate;
    private String guestName;
    private String guestEmail;
    private List<Room> rooms;

    public Reservation(String id, String startDate, String endDate, String guestName,
            String guestEmail, List<Room> rooms) {
        this.id = id;
        this.startDate = startDate;
        this.endDate = endDate;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.rooms = rooms;
    }

    // getters and setters
    public String getId() {
        return id;
    }
    public String getStartDate() {
        return startDate;
    }
    public String getEndDate() {
        return endDate;
    }
    public String getGuestName() {
        return guestName;
    }
    public String getGuestEmail() {
        return guestEmail;
    }
    public List<Room> getRooms() {
        return rooms;
    }
}
