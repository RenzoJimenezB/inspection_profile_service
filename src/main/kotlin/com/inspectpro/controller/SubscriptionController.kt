package com.inspectpro.controller

import com.inspectpro.dto.CheckFeatureRequest
import com.inspectpro.dto.CheckFeatureResponse
import com.inspectpro.dto.StripeWebhookPayload
import com.inspectpro.dto.SubscriptionResponse
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.service.SubscriptionService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class SubscriptionController(private val subscriptionService: SubscriptionService) {

    @GetMapping("/subscriptions/current")
    fun getCurrentSubscription(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<SubscriptionResponse> {
        return ResponseEntity.ok(subscriptionService.getCurrentSubscription(authenticatedUser))
    }

    @PostMapping("/subscriptions/check-feature")
    fun checkFeature(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Valid @RequestBody request: CheckFeatureRequest
    ): ResponseEntity<CheckFeatureResponse> {
        return ResponseEntity.ok(subscriptionService.checkFeature(authenticatedUser, request))
    }

    @PostMapping("/webhooks/stripe")
    fun handleStripeWebhook(
        @Valid @RequestBody payload: StripeWebhookPayload
    ): ResponseEntity<Unit> {
        subscriptionService.handleStripeWebhook(payload)
        return ResponseEntity.ok().build()
    }
}