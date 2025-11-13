package com.businessLogic;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Date;
import java.util.HashMap;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.postgres.PostgresService;
import com.postgres.models.Movies;
import com.postgres.models.Movies.SeatStatus;
import com.topics.SeatRequest;
import com.topics.SeatResponse;

@ExtendWith(MockitoExtension.class)
public class BusinessLogicTest {
	@InjectMocks
	private BusinessLogic businessLogic;

	@Mock
	private PostgresService postgresService;

	private ObjectMapper objectMapper = new ObjectMapper();

	@Test
	@DisplayName("[BUSINESS_LOGIC] Valid SeatRequest & finalize process")
	public void seatRequest(TestInfo testInfo) {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
		String JSON = """
			{
				"topicName": "SeatRequest",
				"correlatorId": 123456,
				"movieName": "The Dark Knight",
				"showtime": "2025-11-10T21:45:00-06:00",
				"seatNumber": "A5"
			}
			""";
		
		SeatRequest request = null;	
		try{
			request = objectMapper.readValue(JSON, SeatRequest.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		LocalDateTime showtime = LocalDateTime.of(2025, 11, 10, 21, 45, 0); // Nov 10 9:45 pm
		Map<String, SeatStatus> map = new HashMap<>();
		map.put("A1", SeatStatus.AVAILABLE);
		map.put("A2", SeatStatus.AVAILABLE);
		map.put("A3", SeatStatus.AVAILABLE);
		map.put("A4", SeatStatus.AVAILABLE);
		map.put("A5", SeatStatus.AVAILABLE);

		Movies movie1 = new Movies();
		movie1.setMovieName("The Dark Knight");
		movie1.setShowtime(showtime);
		movie1.setSeats(map);

		List<Movies> mockMovies = Arrays.asList(movie1);

		Mockito.when(postgresService.findByMovieName(any(String.class)))
			.thenReturn(mockMovies);
		
		Mockito.when(postgresService.save(any(Movies.class)))
			.thenReturn(movie1);

		ResponseEntity<Object> httpResponse = businessLogic.processSeatRequest(request);

		SeatResponse response = null;	
		try{
			@SuppressWarnings("null")
			String body = httpResponse.getBody().toString();
			if (isString(body)) {
				// Handle the string case (you can log it, throw an exception, or handle accordingly)
				System.out.println("\n" + httpResponse.getBody());
				// Optionally, create a default or error response here
			} else {
				response = objectMapper.readValue(body, SeatResponse.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(response);
		Assertions.assertEquals("The Dark Knight", response.getMovieName());
		Assertions.assertEquals("HOLDING", response.getStatus().value());
		Assertions.assertEquals("A5", response.getSeatNumber());

		// Finializing the booking
		map.replace("A5", SeatStatus.BOOKED);
		movie1.setSeats(map);
		Mockito.when(postgresService.save(any(Movies.class)))
			.thenReturn(movie1);
		ResponseEntity<String> finalizeResponse = businessLogic.finalizeSeatBooking(request.getCorrelatorId());

		String status = null;	
		try{
			status = finalizeResponse.getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(status);
		Assertions.assertEquals(SeatResponse.Status.BOOKED.value(), status);
	}

	@Test
	@DisplayName("[BUSINESS_LOGIC] Invalid SeatRequest (movie name)")
	public void badSeatRequest1(TestInfo testInfo) {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
		String JSON = """
			{
				"topicName": "SeatRequest",
				"correlatorId": 123456,
				"movieName": "The Dark Knight",
				"showtime": "2025-11-10T21:45:00-06:00",
				"seatNumber": "A5"
			}
			""";
		
		SeatRequest request = null;	
		try{
			request = objectMapper.readValue(JSON, SeatRequest.class);
		} catch (Exception e) {
			e.printStackTrace();
		}

		LocalDateTime showtime = LocalDateTime.of(2025, 11, 10, 21, 45, 0); // Nov 10 9:45 pm
		Map<String, SeatStatus> map = new HashMap<>();
		map.put("A1", SeatStatus.AVAILABLE);
		map.put("A2", SeatStatus.AVAILABLE);
		map.put("A3", SeatStatus.AVAILABLE);
		map.put("A4", SeatStatus.AVAILABLE);
		map.put("A5", SeatStatus.AVAILABLE);

		Movies movie1 = new Movies();
		movie1.setMovieName("One Piece");
		movie1.setShowtime(showtime);
		movie1.setSeats(map);

		List<Movies> mockMovies = new ArrayList<>();

		Mockito.when(postgresService.findByMovieName(any(String.class)))
			.thenReturn(mockMovies);

		ResponseEntity<Object> httpResponse = businessLogic.processSeatRequest(request);

		SeatResponse response = null;	
		String log = "";
		try{
			@SuppressWarnings("null")
			String body = httpResponse.getBody().toString();
			if (isString(body)) {
				// Handle the string case (you can log it, throw an exception, or handle accordingly)
				System.out.println("\n" + httpResponse.getBody());
				log = httpResponse.getBody().toString();
				// Optionally, create a default or error response here
			} else {
				response = objectMapper.readValue(body, SeatResponse.class);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNull(response);
		Assertions.assertEquals("No movies found with the title The Dark Knight", log);
	}

	@Test
	@DisplayName("[BUSINESS_LOGIC] Invalid SeatRequest finalize")
	public void badSeatRequest2(TestInfo testInfo) {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");

		ResponseEntity<String> finalizeResponse = businessLogic.finalizeSeatBooking(111);
		String status = null;	
		try{
			status = finalizeResponse.getBody();
		} catch (Exception e) {
			e.printStackTrace();
		}

		assertNotNull(status);
		Assertions.assertEquals("Internal Error No held seat found for correlatorId", status);
	}

	private boolean isString(String responseBody) {
		// Check if the response is a simple string (you may need more specific checks depending on your use case)
		return responseBody != null && responseBody.length() > 0 && responseBody.charAt(0) != '{';
	}
}
