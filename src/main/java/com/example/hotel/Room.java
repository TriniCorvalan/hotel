package com.example.hotel;

public class Room {
    private String number;
    private String price;
    private String capacity;

    public Room(String number, String price, String capacity) {
        this.number = number;
        this.price = price;
        this.capacity = capacity;
    }

    // getters and setters
    public String getNumber() {
        return number;
    }
    public String getCapacity() {
        return capacity;
    }
    public String getPrice() {
        return price;
    }
}
