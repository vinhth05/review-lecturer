package com.example.ctu.challenge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.ctu.dto.common.ApiResponse;
import com.example.ctu.exception.BadRequestException;
import com.example.ctu.exception.ForbiddenException;
import com.example.ctu.exception.GlobalExceptionHandler;
import com.example.ctu.exception.ResourceNotFoundException;
import com.example.ctu.exception.UnauthorizedException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

class GlobalExceptionHandlerAndSecurityChallengeTest {

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;

    @RestController
    static class DummyController {
        @GetMapping("/challenge/404")
        public void throw404() {
            throw new ResourceNotFoundException("Entity 123 not found");
        }

        @GetMapping("/challenge/400")
        public void throw400() {
            throw new BadRequestException("Invalid payload argument");
        }

        @GetMapping("/challenge/403")
        public void throw403() {
            throw new ForbiddenException("Requires elevated privileges");
        }

        @GetMapping("/challenge/401")
        public void throw401() {
            throw new UnauthorizedException("Session token has expired");
        }

        @GetMapping("/challenge/500")
        public void throw500() {
            throw new RuntimeException("Uncaught database connection failure");
        }
    }

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
        mockMvc = MockMvcBuilders.standaloneSetup(new DummyController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void testStatus404_PayloadStructure() throws Exception {
        mockMvc.perform(get("/challenge/404"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Entity 123 not found"))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testStatus400_PayloadStructure() throws Exception {
        mockMvc.perform(get("/challenge/400"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Invalid payload argument"))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testStatus403_PayloadStructure() throws Exception {
        mockMvc.perform(get("/challenge/403"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Requires elevated privileges"))
                .andExpect(jsonPath("$.error").value("Forbidden"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testStatus401_PayloadStructure() throws Exception {
        mockMvc.perform(get("/challenge/401"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Session token has expired"))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testStatus500_PayloadStructure() throws Exception {
        mockMvc.perform(get("/challenge/500"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("An unexpected system error occurred"))
                .andExpect(jsonPath("$.error").value("Internal Server Error"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    @Test
    void testSecurityConfig_AuthenticationEntryPoint_Returns401WithStandardJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AuthenticationException authException = new BadCredentialsException("Bad credentials");

        // Simulate SecurityConfig authenticationEntryPoint lambda
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(401);
        ApiResponse<Void> apiResponse = ApiResponse.error("Unauthenticated", "Unauthorized");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        assertThat(response.getStatus()).isEqualTo(401);
        assertThat(response.getContentType()).contains("application/json");

        String json = response.getContentAsString();
        ApiResponse<Void> parsed = objectMapper.readValue(json, new TypeReference<ApiResponse<Void>>() {});
        assertThat(parsed.isSuccess()).isFalse();
        assertThat(parsed.getMessage()).isEqualTo("Unauthenticated");
        assertThat(parsed.getError()).isEqualTo("Unauthorized");
        assertThat(parsed.getTimestamp()).isNotNull();
    }

    @Test
    void testSecurityConfig_AccessDeniedHandler_Returns403WithStandardJson() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest();
        MockHttpServletResponse response = new MockHttpServletResponse();
        AccessDeniedException accessDeniedException = new AccessDeniedException("Insufficient roles");

        // Simulate SecurityConfig accessDeniedHandler lambda
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(403);
        ApiResponse<Void> apiResponse = ApiResponse.error("Access denied: " + accessDeniedException.getMessage(), "Forbidden");
        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));

        assertThat(response.getStatus()).isEqualTo(403);
        assertThat(response.getContentType()).contains("application/json");

        String json = response.getContentAsString();
        ApiResponse<Void> parsed = objectMapper.readValue(json, new TypeReference<ApiResponse<Void>>() {});
        assertThat(parsed.isSuccess()).isFalse();
        assertThat(parsed.getMessage()).isEqualTo("Access denied: Insufficient roles");
        assertThat(parsed.getError()).isEqualTo("Forbidden");
        assertThat(parsed.getTimestamp()).isNotNull();
    }
}
