package com.example.ctu.challenge;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.ctu.dto.common.ApiResponse;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class ApiResponseChallengeTest {

    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Test
    void testSuccessResponseSerialization_OmitsErrorField() throws Exception {
        ApiResponse<String> response = ApiResponse.success("Operation successful", "payload-data");
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"message\":\"Operation successful\"");
        assertThat(json).contains("\"data\":\"payload-data\"");
        assertThat(json).contains("\"timestamp\"");
        assertThat(json).doesNotContain("\"error\"");
    }

    @Test
    void testSuccessResponseSerialization_NoData_OmitsErrorField() throws Exception {
        ApiResponse<Void> response = ApiResponse.success("Success no payload", null);
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":true");
        assertThat(json).contains("\"message\":\"Success no payload\"");
        assertThat(json).doesNotContain("\"error\"");
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    void testErrorResponseSerialization_IncludesErrorFieldAndOmitsData() throws Exception {
        ApiResponse<Void> response = ApiResponse.error("Resource missing", "Not Found");
        String json = objectMapper.writeValueAsString(response);

        assertThat(json).contains("\"success\":false");
        assertThat(json).contains("\"message\":\"Resource missing\"");
        assertThat(json).contains("\"error\":\"Not Found\"");
        assertThat(json).contains("\"timestamp\"");
        assertThat(json).doesNotContain("\"data\"");
    }

    @Test
    void testDeserialization_SuccessResponse() throws Exception {
        String json = "{\"success\":true,\"message\":\"OK\",\"data\":\"hello\",\"timestamp\":\"2026-08-06T10:00:00Z\"}";
        ApiResponse<String> response = objectMapper.readValue(json, new TypeReference<ApiResponse<String>>() {});

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getMessage()).isEqualTo("OK");
        assertThat(response.getData()).isEqualTo("hello");
        assertThat(response.getError()).isNull();
        assertThat(response.getTimestamp()).isEqualTo(Instant.parse("2026-08-06T10:00:00Z"));
    }

    @Test
    void testDeserialization_ErrorResponse() throws Exception {
        String json = "{\"success\":false,\"message\":\"Access denied\",\"error\":\"Forbidden\",\"timestamp\":\"2026-08-06T10:00:00Z\"}";
        ApiResponse<Void> response = objectMapper.readValue(json, new TypeReference<ApiResponse<Void>>() {});

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getMessage()).isEqualTo("Access denied");
        assertThat(response.getError()).isEqualTo("Forbidden");
        assertThat(response.getData()).isNull();
        assertThat(response.getTimestamp()).isEqualTo(Instant.parse("2026-08-06T10:00:00Z"));
    }
}
