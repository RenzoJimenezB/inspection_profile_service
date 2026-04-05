package com.inspectpro.controller

import com.inspectpro.dto.AuthRequest
import com.inspectpro.dto.AuthResponse
import com.inspectpro.dto.RefreshRequest
import com.inspectpro.dto.RefreshResponse
import com.inspectpro.service.AuthService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
@Tag(
    name = "Authentication",
    description = "User registration, login and token refresh"
)
class AuthController(private val authService: AuthService) {

    @Operation(
        summary = "Register a new user",
        description = "Register a new user with a default BASIC profile and subscription"
    )
    @PostMapping("/register")
    fun register(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request))
    }

    @Operation(
        summary = "Login",
        description = "Authenticates user and returns JWT access and refresh tokens"
    )
    @PostMapping("/login")
    fun login(@Valid @RequestBody request: AuthRequest): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(authService.login(request))
    }

    @Operation(
        summary = "Refresh token",
        description = "Issues new access and refresh tokens using a valid refresh token"
    )
    @PostMapping("/refresh")
    fun refresh(@Valid @RequestBody request: RefreshRequest): ResponseEntity<RefreshResponse> {
        return ResponseEntity.ok(authService.refresh(request))
    }
}