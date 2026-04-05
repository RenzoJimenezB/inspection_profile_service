package com.inspectpro.dto

import com.inspectpro.model.CredentialStatus
import com.inspectpro.model.CredentialType
import jakarta.validation.constraints.Future
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant
import java.time.LocalDate

data class CredentialRequest(
    @field:NotBlank(message = "Credential type is required")
    val type: CredentialType,

    @field:NotBlank(message = "Issuer is required")
    val issuer: String,

    @field:NotBlank(message = "License number is required")
    val licenseNumber: String,

    @field:NotNull(message = "Expiry date is required")
    @field:Future(message = "Expiry date must be in the future")
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
    var status: CredentialStatus
)