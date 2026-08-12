package com.example.ctu.entity.enums;

/**
 * Lifecycle of a review. Keeping this explicit avoids treating a rejected
 * review as merely "not approved" and makes moderation transitions auditable.
 */
public enum ReviewStatus {
    PENDING,
    APPROVED,
    REJECTED
}
