package com.postgres;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.postgres.models.Movies;
import jakarta.transaction.Transactional;
import java.util.List;

@Service
public class PostgresService {

    @Autowired
    private SeatRepository seatRepository;

    public List<Movies> findByMovieName(String movieName) {
        return seatRepository.findByMovieName(movieName);
    }

    @Transactional
    public Movies save(Movies movie) {
        return seatRepository.save(movie);
    }
}
