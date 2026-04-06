package com.inspectpro.dto

import com.inspectpro.model.ProfileType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.Instant

data class ProfileResponse(
    val id: Long,
    val displayName: String,
    val type: ProfileType,
    val isActive: Boolean,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class UpdateProfileRequest(
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name cannot exceed 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9 -]+$",
        message = "Display name can only contain letters, numbers, spaces and hyphens"
    )
    val displayName: String,
)

data class CreateProfileRequest(
    @field:NotBlank(message = "Display name is required")
    @field:Size(max = 100, message = "Display name cannot exceed 100 characters")
    @field:Pattern(
        regexp = "^[a-zA-Z0-9-]+$",
        message = "Display name can only contain letters, numbers, spaces and hyphens"
    )
    val displayName: String,
)