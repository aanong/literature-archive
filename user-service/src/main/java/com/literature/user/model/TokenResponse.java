package com.literature.user.model;

public record TokenResponse(String token, long expiresIn) {
}
