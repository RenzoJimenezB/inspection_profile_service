package com.inspectpro.dto

import com.inspectpro.model.SubscriptionTier
import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class SubscriptionResponse(
    val id: Long,
    val tier: SubscriptionTier,
    val isActive: Boolean,
    val startedAt: Instant,
    val expiresAt: Instant?,
    val createdAt: Instant,
    val updatedAt: Instant,
)

data class CheckFeatureRequest(
    @field:NotBlank(message = "Feature is required")
    @Schema(example = "PDF_EXPORT")
    val feature: String,
)

data class CheckFeatureResponse(
    val feature: String,
    val allowed: Boolean,
)

data class StripeWebhookPayload(
    @field:NotBlank(message = "Event type is required")
    @Schema(example = "subscription.updated")
    val eventType: String,

    @field:NotNull(message = "User ID is required")
    @Schema(example = "2")
    var userId: Long,

    @field:NotNull(message = "Tier is required")
    @Schema(example = "ENHANCED")
    var tier: SubscriptionTier,
)