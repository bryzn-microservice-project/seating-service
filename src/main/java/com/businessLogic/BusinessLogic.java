package com.businessLogic;

import java.util.HashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
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
    private RestClient paymentServiceClient = RestClient.create();

    private HashMap<String, RestClient> restRouter = new HashMap<>();
    private HashMap<RestClient, String> restEndpoints = new HashMap<>();

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

        restRouter.put("PaymentRequest", paymentServiceClient);
        restEndpoints.put(paymentServiceClient, "http://payment-service:8084/api/v1/processTopic");
        LOG.info("Sucessfully mapped the topics to their respective microservices...");
    }

    /*
     * Request handlers for the various topics, which communicate through REST
     * clients
     */
    public ResponseEntity<String> processSeatRequest(SeatRequest seatRequest) {
        LOG.info("Received a SeatRequest. Sending the topic to the [Payment Service]");
        if (sendPaymentRequest(seatRequest.getPayment()).getStatusCode() == HttpStatusCode.valueOf(200)) {
            LOG.info("PaymentRequest was successfully processed!");
        } else {
            LOG.error("Failed to process the payment request...");
            return ResponseEntity.status(500).body("Internal Error Failed to process SeatRequest");
        }

        Seat seat = new Seat(seatRequest.getShowtime().toString(), seatRequest.getSeatNumber());
        Seat postgresSaveResponse = postgresService.save(seat);
        Status seatStatus = postgresSaveResponse.getId() != null ? Status.CONFIRMED : Status.FAILED;
        LOG.info("SeatRequest processed with status: " + seatStatus);
        SeatResponse seatResponse = createSeatResponse(seatRequest, seatStatus);

        restRouter.get("SeatResponse")
                .post()
                .uri(restEndpoints.get(restRouter.get("SeatResponse")))
                .contentType(MediaType.APPLICATION_JSON)
                .body(seatResponse)
                .retrieve()
                .toBodilessEntity();

        return postgresSaveResponse.getId() != null ? ResponseEntity.ok("Entity was created/updated successully")
                : ResponseEntity.status(500).body("Inernal Error Failed to process SeatRequest");
    }

    private SeatResponse createSeatResponse(SeatRequest seatRequest, Status seatStatus) {
        LOG.info("Creating a SeatResponse... with status: " + seatStatus);
        SeatResponse seatResponse = new SeatResponse();
        seatResponse.setTopicName("SeatResponse");
        seatResponse.setCorrelatorId(seatRequest.getCorrelatorId());
        seatResponse.setStatus(seatStatus);
        return seatResponse;
    }

    private ResponseEntity<String> sendPaymentRequest(PaymentRequest paymentRequest) {
        LOG.info("Sending a PaymentRequest to the [Payment Service]");
        return restRouter.get("PaymentRequest")
                .post()
                .uri(restEndpoints.get(restRouter.get("PaymentRequest")))
                .contentType(MediaType.APPLICATION_JSON)
                .body(paymentRequest)
                .retrieve()
                .toEntity(String.class);
    }

}
