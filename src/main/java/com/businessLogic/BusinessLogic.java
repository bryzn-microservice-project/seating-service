package com.businessLogic;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.topics.*;
import com.topics.SeatResponse.Status;
import com.postgres.PostgresService;
import com.postgres.models.Seat;

/*
 * Handles the business logic for processing various topics and utilizes 
 * REST clients to communicate with other microservices.
 */
@Service
public class BusinessLogic {
    private static final Logger LOG = LoggerFactory.getLogger(BusinessLogic.class);
    public final PostgresService postgresService;

    // REST Clients to communicate with other microservices
    private RestClient apiGatewayClient = RestClient.create();

    private HashMap<String, RestClient> restRouter = new HashMap<>();
    private HashMap<RestClient, String> restEndpoints = new HashMap<>();

    // Hash Map to keep track of seats held to their correlatorId
    HashMap<Integer, Seat> heldSeats = new HashMap<>();

    public BusinessLogic(PostgresService postgresService) {
        this.postgresService = postgresService;
        mapTopicsToClient();
    }

    /*
     * Method to map topics to their respective microservices and endpoints
     * # api-gateway:8081
     * # movie-service:8082
     * # notification-service:8083
     * # payment-service:8084
     * # seating-service:8085
     * # user-management-service:8086
     * # gui-service:8087
     */
    public void mapTopicsToClient() {
        restRouter.put("SeatResponse", apiGatewayClient);
        restEndpoints.put(apiGatewayClient, "http://api-gateway:8081/api/v1/processTopic");
    }

    /*
     * Request handlers for the various topics, which communicate through REST
     * clients
     */
    public ResponseEntity<String> processSeatRequest(SeatRequest seatRequest) {
        // Seat(LocalDateTime showtime, String seatNumber, SeatingStatus seatingStatus)
        LocalDateTime timeConversion = seatRequest.getShowtime().toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();

        Seat seat = new Seat(timeConversion, seatRequest.getSeatNumber(), Seat.SeatingStatus.HOLDING);
        Seat postgresSaveResponse = postgresService.save(seat);
        Status seatStatus = postgresSaveResponse.getId() != null ? Status.HOLDING : Status.FAILED;

        if(seatStatus == Status.HOLDING) {
            heldSeats.put(seatRequest.getCorrelatorId(), seat);
        }

        LOG.info("SeatRequest processed with status: " + seatStatus);
        SeatResponse seatResponse = createSeatResponse(seatRequest, seatStatus);

        return postgresSaveResponse.getId() != null ? ResponseEntity.ok(seatResponse.toString())
                : ResponseEntity.status(500).body("Inernal Error Failed to process SeatRequest");
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
    public ResponseEntity<String> finalizeSeatBooking(int correlatorId) {
        LOG.info("Finalizing seat booking for correlatorId: " + correlatorId);
        Seat seat = heldSeats.get(correlatorId);

        Seat postgresUpdateResponse = postgresService.save(seat);
        if(postgresUpdateResponse.getSeatingStatus() == Seat.SeatingStatus.BOOKED)
        {
            LOG.info("Seat booking finalized successfully for correlatorId: " + correlatorId);
            heldSeats.remove(correlatorId);
        } else {
            LOG.error("Failed to finalize seat booking for correlatorId: " + correlatorId);
        }
        return postgresUpdateResponse.getSeatingStatus() == Seat.SeatingStatus.BOOKED ? 
            ResponseEntity.ok("Seat booking finalized successfully!") : 
            ResponseEntity.status(500).body("Inernal Error Failed to finalize seat booking");
    }

}
