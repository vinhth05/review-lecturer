package com.example.ctu.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.ctu.config.AppProperties;
import com.example.ctu.entity.RefreshToken;
import com.example.ctu.entity.User;
import com.example.ctu.repository.RefreshTokenRepository;

@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock private RefreshTokenRepository repository;
    @Mock private AppProperties properties;

    @Test
    void createRefreshToken_persistsOnlyHashAndReturnsHighEntropyToken() {
        User user = User.builder().id(7L).build();
        when(properties.jwt()).thenReturn(new AppProperties.Jwt("unused", 15, 7));
        when(repository.save(any(RefreshToken.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        RefreshTokenService service = new RefreshTokenService(repository, properties);

        RefreshTokenService.RefreshTokenGrant grant = service.createRefreshToken(user);

        ArgumentCaptor<RefreshToken> captor = ArgumentCaptor.forClass(RefreshToken.class);
        verify(repository).save(captor.capture());
        assertThat(grant.token()).hasSizeGreaterThanOrEqualTo(40);
        assertThat(captor.getValue().getTokenHash())
                .hasSize(64)
                .doesNotContain(grant.token());
    }
}
