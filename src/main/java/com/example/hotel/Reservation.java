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
    public void setId(String id) {
        this.id = id;
    }
    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }
    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }
    public void setGuestName(String guestName) {
        this.guestName = guestName;
    }
    public void setGuestEmail(String guestEmail) {
        this.guestEmail = guestEmail;
    }
    public void setRooms(List<Room> rooms) {
        this.rooms = rooms;
    }
}
