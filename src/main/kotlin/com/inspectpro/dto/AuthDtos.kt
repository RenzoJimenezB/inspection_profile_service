package com.inspectpro.dto

import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size

data class AuthRequest(
    @field:Email(message = "Invalid email format")
    @field:NotBlank(message = "Email is required")
    val email: String,

    @field:NotBlank(message = "Please provide a password")
    @field:Size(min = 8, message = "Password must be at least 8 characters long")
    val password: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
)

data class RefreshRequest(
    @field:NotBlank(message = "Refresh token is required")
    val refreshToken: String,
)

data class RefreshResponse(
    val accessToken: String,
    val refreshToken: String,
)