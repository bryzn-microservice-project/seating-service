package com.businessLogic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postgres.PostgresService;
import com.postgres.models.Movies;
import com.postgres.models.Movies.SeatStatus;
import com.topics.SeatRequest;
import com.topics.SeatResponse;
import com.topics.SeatResponse.Status;
import jakarta.transaction.Transactional;

/*
 * Handles the business logic for processing various topics and utilizes 
 * REST clients to communicate with other microservices.
 */
@Service
public class BusinessLogic {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessLogic.class);
    public final PostgresService postgresService;

    // Hash Map to keep track of seats held to their correlatorId
    HashMap<Integer, Movies> movieMap = new HashMap<>();

    // Hash Map to keep track of held seats before finalizing booking
    HashMap<Movies, String> heldSeats = new HashMap<>();

    public BusinessLogic(PostgresService postgresService) {
        this.postgresService = postgresService;
    }

    /*
     * Request handlers for the various topics, which communicate through REST
     * clients
     */
    @Transactional
    public ResponseEntity<String> processSeatRequest(SeatRequest seatRequest) {
        LOG.info("Atempting to reserve a seat for the movie: " + seatRequest.getMovieName() +
                ", seat number: " + seatRequest.getSeatNumber() +
                ", showtime: " + seatRequest.getShowtime());

        LocalDateTime timeConversion = seatRequest.getShowtime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        List<Movies> movieCheck = postgresService.findByMovieName(seatRequest.getMovieName());
        Movies movie = null;
        Status logStatus = Status.FAILED;
        String logMessage = "Inernal Error Failed to process SeatRequest";
        for(Movies m : movieCheck) {
            Map<String, SeatStatus> seats = m.getSeats();
            SeatStatus status = seats.get(seatRequest.getSeatNumber());
            if(seats.get(seatRequest.getSeatNumber()) == null) {
                LOG.info("Seat " + seatRequest.getSeatNumber() + " does not exist for movie " 
                    + seatRequest.getMovieName() + " at " + seatRequest.getShowtime());
                logMessage = "Seat " + seatRequest.getSeatNumber() + " does not exist.";
                continue;
            }
            else if(m.getShowtime().isEqual(timeConversion) && status == SeatStatus.AVAILABLE) {
                LOG.info("Seat " + seatRequest.getSeatNumber() + " is available. Proceeding to hold the seat...");
                seats.put(seatRequest.getSeatNumber(), SeatStatus.HOLDING);
                m.setSeats(seats);
                movie = postgresService.save(m); 

                // update the hashmap to track the movie and seat.
                movieMap.put(seatRequest.getCorrelatorId(), movie);
                heldSeats.put(movie, seatRequest.getSeatNumber());
                LOG.info("Mappings updated for corelatorId: " + seatRequest.getCorrelatorId() + " to movie ID: " + movie.getId() 
                    + " and seat number: " + seatRequest.getSeatNumber());

                logStatus = Status.HOLDING;
                LOG.info("Reserving seat " + seatRequest.getSeatNumber() + " for movie " 
                    + seatRequest.getMovieName() + " at " + seatRequest.getShowtime());                
                logMessage = "Seat " + seatRequest.getSeatNumber() + " is now being held.";
                break;
            }
            else{
                LOG.info("Seat " + seatRequest.getSeatNumber() + " is already booked/held for movie " 
                    + seatRequest.getMovieName() + " at " + seatRequest.getShowtime());
                logMessage = "Seat " + seatRequest.getSeatNumber() + " is already booked/held.";
            }
        }

        LOG.info("Initial SeatRequest processed with status: " + movie != null ? "HOLDING" : "FAILED");
        SeatResponse seatResponse = createSeatResponse(seatRequest, logStatus);
        return movie != null ? ResponseEntity.ok(toJson(seatResponse))
                : ResponseEntity.status(500).body(logMessage);
    }

    private SeatResponse createSeatResponse(SeatRequest seatRequest, Status seatStatus) {
        LOG.info("Creating a SeatResponse... with status: " + seatStatus);
        SeatResponse seatResponse = new SeatResponse();
        seatResponse.setTopicName("SeatResponse");
        seatResponse.setCorrelatorId(seatRequest.getCorrelatorId());
        seatResponse.setStatus(seatStatus);
        seatResponse.setMovieName(seatRequest.getMovieName());
        seatResponse.setSeatNumber(seatRequest.getSeatNumber());
        seatResponse.setShowtime(seatRequest.getShowtime());
        seatResponse.setTimestamp(new Date());
        return seatResponse;
    }

    // Method to finalize seat booking after payment confirmation received from the
    // service orchestrator. Uses the held seat mapping to update the seat status to BOOKED
    public ResponseEntity<String> finalizeSeatBooking(Integer correlatorId) {
        LOG.info("Finalizing seat booking for correlatorId: " + correlatorId);
        Movies movie = movieMap.get(correlatorId);

        if(movie == null) {
            LOG.error("No held seat found for correlatorId: " + correlatorId);
            return ResponseEntity.status(500).body("Inernal Error No held seat found for correlatorId");
        }

        String seat = heldSeats.get(movie);
        Map<String, SeatStatus> seats = movie.getSeats();
        seats.put(seat, SeatStatus.BOOKED);
        movie.setSeats(seats);
        
        Movies bookingResponse = postgresService.save(movie); 
        if(bookingResponse.getSeats().get(seat) == SeatStatus.BOOKED)
        {
            LOG.info("Seat booking finalized successfully for correlatorId: " + correlatorId);

            // remove mapping
            movieMap.remove(correlatorId);
            heldSeats.remove(movie);
        } else {
            LOG.error("Failed to finalize seat booking for correlatorId: " + correlatorId);
        }

        return bookingResponse != null ? 
            ResponseEntity.ok(Status.BOOKED.value()) : 
            ResponseEntity.status(500).body(Status.FAILED.value());
    }

    // Helper method to serialize an object to JSON string
    private String toJson(Object obj) {
        try {
            // Use Jackson ObjectMapper to convert the object to JSON
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(obj);  // Convert object to JSON string
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return "{\"error\":\"Error processing JSON\"}";
        }
    }
}
