package com.inspectpro.dto

import com.inspectpro.model.CredentialStatus
import com.inspectpro.model.CredentialType
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

data class CredentialRequest(
    @field:NotNull(message = "Credential type is required")
    @Schema(example = "HVAC_LICENSE")
    var type: CredentialType,

    @field:NotBlank(message = "Issuer is required")
    @Schema(example = "State Licensing Board")
    val issuer: String,

    @field:NotBlank(message = "License number is required")
    @Schema(example = "HVAC-12345")
    val licenseNumber: String,

    @field:NotNull(message = "Expiry date is required")
    @field:Future(message = "Expiry date must be in the future")
    @Schema(example = "2027-12-31")
    var expiryDate: LocalDate
)

data class CredentialResponse(
    val id: Long,
    val profileId: Long,
    val type: CredentialType,
    val issuer: String,
    val licenseNumber: String,
    val expiryDate: LocalDate,
    val status: CredentialStatus,
    val createdAt: Instant,
    val updatedAt: Instant
)

data class UpdateCredentialStatusRequest(
    @field:NotNull(message = "Status is required")
    @Schema(example = "APPROVED")
    var status: CredentialStatus
)