package com.schemaValidator;

import java.io.InputStream;
import java.net.URL;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ResourceLoader;
import com.SchemaService;
import com.schema.SchemaValidator;  

@SpringBootTest(classes = SchemaValidator.class)
@EnableAutoConfiguration(exclude = {DataSourceAutoConfiguration.class})
class SchemaValidatorTest {
    @Autowired
    private ResourceLoader resourceLoader;

    private SchemaValidator schemaValidator;

    @BeforeEach
    void setup() {
        schemaValidator = new SchemaValidator(resourceLoader);
    }

    @Test
    @DisplayName("[SCHEMA] Valid SeatRequest 1")
    void testSeatRequest1(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 123456,
                "movieName": "The Dark Knight",
                "showtime": "2025-11-10T21:45:00-06:00",
                "seatNumber": "A5"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertTrue(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Valid SeatRequest 1")
    void testSeatRequest2(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 11,
                "movieName": "One Piece",
                "showtime": "2025-05-10T21:45:00-06:00",
                "seatNumber": "B3"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertTrue(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Valid SeatRequest 1")
    void testSeatRequest3(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 12234,
                "movieName": "Demon Slayer",
                "showtime": "2026-05-10T21:45:00-06:00",
                "seatNumber": "C5"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertTrue(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Invalid SeatRequest (bad showtime)")
    void testBadSeatRequest1(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 12234,
                "movieName": "Demon Slayer",
                "showtime": "2026-55-10T21:45:00-06:00",
                "seatNumber": "C5"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertFalse(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Invalid SeatRequest (bad showtime month)")
    void testBadSeatRequest2(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 12234,
                "movieName": "Demon Slayer",
                "showtime": "2026-2-10T21:45:00-06:00",
                "seatNumber": "C5"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertFalse(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Invalid SeatRequest (bad seat)")
    void testBadSeatRequest3(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 12234,
                "movieName": "One Piece",
                "showtime": "2026-05-10T21:45:00-06:00",
                "seatNumber": "124"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertFalse(validate(topicName, validJson));
    }

    @Test
    @DisplayName("[SCHEMA] Invalid SeatRequest (no movie)")
    void testBadSeatRequest4(TestInfo testInfo) throws Exception {
        System.out.println("\n-----------Running: " + testInfo.getDisplayName() + "-----------");
        // JSON that satisfies the schema
        JSONObject validJson = new JSONObject("""
            {
                "topicName": "SeatRequest",
                "correlatorId": 12234,
                "showtime": "2026-55-10T21:45:00-06:00",
                "seatNumber": "124"
            }
        """);

        // Testing with a dynamically loaded schema from the SchemaService
        String topicName = "SeatRequest";  

        // Assert the validation result (assuming valid schema)
        Assertions.assertFalse(validate(topicName, validJson));
    }

    private boolean validate(String topicName, JSONObject validJson)
    {
        URL schemaUrl = getClass().getClassLoader().getResource(SchemaService.getPathFor(topicName));

        // Check if the schema is found
        if (schemaUrl == null) {
            throw new RuntimeException("Schema not found for topic: " + topicName);
        }

        // Load schema stream from resource
        InputStream schemaStream = schemaValidator.getSchemaStream(SchemaService.getPathFor(topicName));

        if (schemaStream == null) {
            System.out.println("No schema found for topic: " + topicName);
        }

        // Validate the JSON using the schema stream
        return schemaValidator.validateJson(schemaStream, validJson);
    }
}
