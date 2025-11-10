package com.postgres.models;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.io.IOException;
import java.util.Map;

@Converter
public class JsonConverter implements AttributeConverter<Map<String, Movies.SeatStatus>, String> {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public String convertToDatabaseColumn(Map<String, Movies.SeatStatus> attribute) {
        try {
            if (attribute == null) {
                return null;
            }
            // Convert the Map to a JSON string for storing in the JSONB column
            return objectMapper.writeValueAsString(attribute);
        } catch (IOException e) {
            throw new IllegalArgumentException("Error converting Map to JSON", e);
        }
    }

    @Override
    public Map<String, Movies.SeatStatus> convertToEntityAttribute(String dbData) {
        try {
            if (dbData == null || dbData.isEmpty()) {
                return null;
            }
            // Convert the JSON string back to the Map
            return objectMapper.readValue(dbData, new TypeReference<Map<String, Movies.SeatStatus>>() {});
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading JSON to Map", e);
        }
    }
}
