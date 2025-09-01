package com.postgres.models;

import java.time.LocalDateTime;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;

@Entity
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String seatNumber;
    private LocalDateTime showtime;
    private SeatingStatus seatingStatus;

    public static enum SeatingStatus {
        AVAILABLE,
        HOLDING,
        BOOKED
    }

    // for JPA only, no use
    public Seat() {
    }

    public Seat(LocalDateTime showtime, String seatNumber, SeatingStatus seatingStatus) {
        this.showtime = showtime;
        this.seatNumber = seatNumber;
        this.seatingStatus = seatingStatus;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDateTime getShowtime() {
        return showtime;
    }

    public void setShowtime(LocalDateTime showtime) {
        this.showtime = showtime;
    }

    public String getSeatNumber() {
        return seatNumber;
    }

    public void setSeatNumber(String seatNumber) {
        this.seatNumber = seatNumber;
    }

    public SeatingStatus getSeatingStatus() {
        return seatingStatus;
    }

    public void setSeatingStatus(SeatingStatus seatingStatus) {
        this.seatingStatus = seatingStatus;
    }
}