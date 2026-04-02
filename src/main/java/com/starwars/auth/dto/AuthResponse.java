package com.starwars.auth.dto;

import java.util.Date;

/**
 * Response returned after a successful register or login operation.
 *
 * @param token     the JWT Bearer token to include in subsequent requests
 * @param expiresAt when the token expires (UTC)
 * @param username  the authenticated user's username
 */
public record AuthResponse(String token, Date expiresAt, String username) {}