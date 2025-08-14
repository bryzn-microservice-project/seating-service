package com.postgres;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.postgres.models.Seat;
import java.util.List;
import java.util.Optional;

@Service
public class PostgresService {

    @Autowired
    private SeatRepository seatRepository;

    public List<Seat> findAll() {
        return seatRepository.findAll();
    }

    public Optional<Seat> findById(Long id) {
        return seatRepository.findById(id);
    }

    // save includes creating and updating
    public Seat save(Seat seat) {
        return seatRepository.save(seat);
    }

    public void deleteById(Long id) {
        seatRepository.deleteById(id);
    }

    public List<Seat> findBySeatNumber(String seatNumber) {
        return seatRepository.findBySeatNumber(seatNumber);
    }

    public List<Seat> findByShowtime(String showTime) {
        return seatRepository.findByShowtime(showTime);
    }
}
