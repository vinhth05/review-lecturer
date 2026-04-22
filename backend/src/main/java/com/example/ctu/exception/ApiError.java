package com.example.ctu.exception;

import java.time.Instant;

public record ApiError(String message, Instant timestamp) {
}
