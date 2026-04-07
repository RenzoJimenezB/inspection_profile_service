package com.inspectpro.dto

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    @Schema(example = "user@inspectpro.com")
    val email: String,

    @field:NotBlank(message = "Please provide a password")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    @Schema(example = "Password123!")
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class RefreshRequest(
    @field:NotBlank(message = "Refresh token is required")
    @Schema(example = "eyJhbGciOiJIUzM4NCJ9...")
    val refreshToken: String,
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)