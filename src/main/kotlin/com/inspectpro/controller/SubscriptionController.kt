package com.inspectpro.controller

import com.inspectpro.dto.CheckFeatureRequest
import com.inspectpro.dto.CheckFeatureResponse
import com.inspectpro.dto.StripeWebhookPayload
import com.inspectpro.dto.SubscriptionResponse
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.service.SubscriptionService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1")
@Tag(
    name = "Subscriptions",
    description = "Subscription management and feature access control"
)
class SubscriptionController(private val subscriptionService: SubscriptionService) {

    @Operation(
        summary = "Get current subscription",
        description = "Returns the active subscription for the authenticated user"
    )
    @GetMapping("/subscriptions/current")
    fun getCurrentSubscription(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(authenticatedUser))
    }

    @Operation(
        summary = "Check feature access",
        description = "Checks if the user's subscription tier allows access to a specific feature"
    )
    @PostMapping("/subscriptions/check-feature")
    fun checkFeature(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Valid @RequestBody request: CheckFeatureRequest
    ): ResponseEntity<CheckFeatureResponse> {
        return ResponseEntity.ok(subscriptionService.checkFeature(authenticatedUser, request))
    }

    @Operation(
        summary = "Stripe webhook",
        description = "Handles Stripe subscription update events (mock implementation)"
    )
    @PostMapping("/webhooks/stripe")
    fun handleStripeWebhook(
        @Valid @RequestBody payload: StripeWebhookPayload
    ): ResponseEntity<Unit> {
        subscriptionService.handleStripeWebhook(payload)
        return ResponseEntity.ok().build()
    }
}