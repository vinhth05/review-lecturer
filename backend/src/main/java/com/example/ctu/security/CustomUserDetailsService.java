package com.example.ctu.security;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.example.ctu.entity.User;
import com.example.ctu.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomUserDetailsService.class);

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        LOGGER.debug("Loading user details for email: {}", username);
        User user = userRepository.findByEmail(username)
                .orElseThrow(() -> {
                    LOGGER.warn("User not found for email: {}", username);
                    return new UsernameNotFoundException("User not found");
                });
        LOGGER.debug("User found: email={}, role={}, verified={}, locked={}", user.getEmail(), user.getRole(), user.isVerified(), user.isLocked());
        return org.springframework.security.core.userdetails.User
            .withUsername(user.getEmail())
            .password(user.getPasswordHash())
            .authorities(List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())))
            .accountLocked(user.isLocked())
            .disabled(false)
            .accountExpired(false)
            .credentialsExpired(false)
            .build();
    }
}
