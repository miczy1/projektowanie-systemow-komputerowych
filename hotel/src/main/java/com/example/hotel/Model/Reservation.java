package com.example.hotel.Model;

public class Reservation {
    private Integer id;
    private String room;
    private String date;
    private String time;
    private String reservedBy;

    public Reservation() { }

    public Reservation(Integer id, String room, String date, String time, String reservedBy) {
        this.id = id;
        this.room = room;
        this.date = date;
        this.time = time;
        this.reservedBy = reservedBy;
    }

    public Integer getId() {
        return id;
    }
    public String getRoom() {
        return room;
    }
    public String getDate() {
        return date;
    }
    public String getTime() {
        return time;
    }
    public String getReservedBy() {
        return reservedBy;
    }
    public void setId(Integer id) {
        this.id = id;
    }
    public void setRoom(String room) {
        this.room = room;
    }
    public void setDate(String date) {
        this.date = date;
    }
    public void setTime(String time) {
        this.time = time;
    }
    public void setReservedBy(String reservedBy) {
        this.reservedBy = reservedBy;
    }
}
