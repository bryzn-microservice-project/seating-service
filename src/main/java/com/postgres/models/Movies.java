package com.postgres.models;

import java.time.LocalDateTime;
import java.util.Map;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.Column;
import jakarta.persistence.Convert;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import com.topics.MovieListRequest.Genre;

@Entity
@Table(name = "movies", schema = "movie_service")
public class Movies {
    public enum SeatStatus {
        AVAILABLE,
        HOLDING,
        BOOKED
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_name", nullable = false)
    private String movieName;

    @Column(name = "showtime", nullable = false)
    private LocalDateTime showtime;

    @Column(columnDefinition = "jsonb")
    @Convert(converter = JsonConverter.class)
    @JdbcTypeCode(SqlTypes.JSON) // important annotation for the jsonb type
    private Map<String, SeatStatus> seats;

    // for JPA only, no use
    public Movies() {
    }

    public Movies(String movieName, LocalDateTime showtime, Genre genre) {
        this.movieName = movieName;
        this.showtime = showtime;   
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMovieName() {
        return movieName;
    }

    public void setMovieName(String movieName) {
        this.movieName = movieName;
    }

    public LocalDateTime getShowtime() {
        return showtime;
    }

    public void setShowtime(LocalDateTime showtime) {
        this.showtime = showtime;
    }

    public Map<String, SeatStatus> getSeats() {
        return seats;
    }

    public void setSeats(Map<String, SeatStatus> seats) {
        this.seats = seats;
    }
}