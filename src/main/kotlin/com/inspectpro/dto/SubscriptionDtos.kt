package com.inspectpro.dto

import com.inspectpro.model.SubscriptionTier
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
    val feature: String,
)

data class CheckFeatureResponse(
    val feature: String,
    val allowed: Boolean,
)

data class StripeWebhookPayload(
    @field:NotBlank(message = "Event type is required")
    val eventType: String,

    @field:NotNull(message = "User ID is required")
    var userId: Long,

    @field:NotNull(message = "Tier is required")
    var tier: SubscriptionTier,
)