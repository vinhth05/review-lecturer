package com.example.ctu.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Component
public class UserSchemaCompatibilityInitializer implements ApplicationRunner {

    private final JdbcTemplate jdbcTemplate;

    public UserSchemaCompatibilityInitializer(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public void run(ApplicationArguments args) {
        Integer columnExists = jdbcTemplate.queryForObject(
                "SELECT COL_LENGTH('users', 'is_locked')",
                Integer.class
        );

        if (columnExists == null) {
            jdbcTemplate.execute("ALTER TABLE [users] ADD is_locked BIT NOT NULL DEFAULT 0");
        }
    }
}
