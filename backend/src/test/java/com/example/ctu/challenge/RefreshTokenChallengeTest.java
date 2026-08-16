package com.example.ctu.challenge;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.Instant;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import com.example.ctu.config.AppProperties;
import com.example.ctu.controller.AuthController;
import com.example.ctu.dto.auth.AuthDtos;
import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;
import com.example.ctu.exception.GlobalExceptionHandler;
import com.example.ctu.exception.UnauthorizedException;
import com.example.ctu.repository.RefreshTokenRepository;
import com.example.ctu.service.AuthService;
import com.example.ctu.service.RefreshTokenService;

@ExtendWith(MockitoExtension.class)
class RefreshTokenChallengeTest {

    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private AppProperties appProperties;
    @InjectMocks private RefreshTokenService refreshTokenService;
    @Mock private AuthService authService;
    @InjectMocks private AuthController authController;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void rotateRefreshToken_nonExistentToken_isUnauthorized() {
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("non-existent-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("does not exist");
    }

    @Test
    void rotateRefreshToken_expiredToken_isUnauthorized() {
        RefreshToken expiredToken = token(false, Instant.now().minusSeconds(3600));
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(expiredToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("expired-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("invalid or expired");
    }

    @Test
    void rotateRefreshToken_revokedToken_isUnauthorized() {
        RefreshToken revokedToken = token(true, Instant.now().plusSeconds(3600));
        when(refreshTokenRepository.findByTokenHashForUpdate(anyString())).thenReturn(Optional.of(revokedToken));

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken("revoked-token"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("invalid or expired");
    }

    @Test
    void refreshTokenEndpoint_expiredToken_returns401() throws Exception {
        AuthDtos.RefreshTokenRequest request = new AuthDtos.RefreshTokenRequest("expired-token");
        when(authService.refreshAccessToken(request))
                .thenThrow(new UnauthorizedException("Refresh token is invalid or expired"));

        mockMvc.perform(post("/auth/refresh-token")
                        .contentType(APPLICATION_JSON)
                        .content("{\"refreshToken\":\"expired-token\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("Refresh token is invalid or expired"))
                .andExpect(jsonPath("$.error").value("Unauthorized"))
                .andExpect(jsonPath("$.timestamp").exists());
    }

    private RefreshToken token(boolean revoked, Instant expiresAt) {
        User user = User.builder().id(1L).email("user@ctu.edu.vn").verified(true).build();
        return RefreshToken.builder()
                .tokenHash("stored-hash")
                .user(user)
                .expiresAt(expiresAt)
                .revoked(revoked)
                .build();
    }
}
