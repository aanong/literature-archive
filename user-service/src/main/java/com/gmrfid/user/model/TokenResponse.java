package com.gmrfid.user.model;

public record TokenResponse(String token, long expiresIn) {
}
