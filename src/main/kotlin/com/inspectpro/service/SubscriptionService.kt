package com.inspectpro.service

import com.inspectpro.dto.CheckFeatureRequest
import com.inspectpro.dto.CheckFeatureResponse
import com.inspectpro.dto.StripeWebhookPayload
import com.inspectpro.dto.SubscriptionResponse
import com.inspectpro.exception.BusinessException
import com.inspectpro.exception.ResourceNotFoundException
import com.inspectpro.model.Subscription
import com.inspectpro.model.SubscriptionTier
import com.inspectpro.repository.SubscriptionRepository
import com.inspectpro.repository.UserRepository
import com.inspectpro.security.AuthenticatedUser
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.YearMonth
import java.util.concurrent.TimeUnit

@Service
class SubscriptionService(
    private val subscriptionRepository: SubscriptionRepository,
    private val userRepository: UserRepository,
    private val redisTemplate: RedisTemplate<String, String>
) {

    fun getCurrentSubscription(authenticatedUser: AuthenticatedUser): SubscriptionResponse {
        val subscription = subscriptionRepository.findByUserIdAndIsActiveTrue(
            authenticatedUser.userId
        ) ?: throw ResourceNotFoundException("No active subscription found")
        return subscription.toResponse()
    }

    fun checkFeature(
        authenticatedUser: AuthenticatedUser,
        request: CheckFeatureRequest
    ): CheckFeatureResponse {
        val subscription = subscriptionRepository.findByUserIdAndIsActiveTrue(
            authenticatedUser.userId
        ) ?: throw ResourceNotFoundException("No active subscription found")

        val allowed = when (request.feature.uppercase()) {
            "PDF_EXPORT" -> subscription.tier != SubscriptionTier.BASIC
            "INSPECTION_CREATE" -> checkInspectionLimit(authenticatedUser.userId, subscription.tier)
            else -> throw BusinessException("Unknown feature: ${request.feature}")
        }
        return CheckFeatureResponse(
            feature = request.feature,
            allowed = allowed
        )
    }

    @Transactional
    fun handleStripeWebhook(payload: StripeWebhookPayload) {
        if (payload.eventType != "subscription.updated") {
            return
        }

        if (payload.tier == SubscriptionTier.BASIC) {
            throw BusinessException("Cannot upgrade to BASIC tier via webhook")
        }

        val user = userRepository.findById(payload.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        val currentSubscription = subscriptionRepository.findByUserIdAndIsActiveTrue(payload.userId)

        currentSubscription?.let {
            it.isActive = false
            subscriptionRepository.save(it)
        }

        subscriptionRepository.save(
            Subscription(
                user = user,
                tier = payload.tier,
                isActive = true,
                startedAt = Instant.now(),
                expiresAt = Instant.now().plusSeconds(2592000)
            )
        )
    }

    private fun checkInspectionLimit(userId: Long, tier: SubscriptionTier): Boolean {
        val limit = when (tier) {
            SubscriptionTier.BASIC -> 5
            SubscriptionTier.ENHANCED -> 50
            SubscriptionTier.PROFESSIONAL -> return true
        }

        val key = "inspections:$userId:${YearMonth.now()}"
        val current = redisTemplate.opsForValue().increment(key) ?: return false

        return if (current > limit) {
            redisTemplate.opsForValue().decrement(key)
            false
        } else {
            val daysUntilEndOfMonth = YearMonth.now().lengthOfMonth() - java.time.LocalDate.now().dayOfMonth.toLong()
            redisTemplate.expire(key, daysUntilEndOfMonth + 1, TimeUnit.DAYS)
            true
        }
    }

    private fun Subscription.toResponse() = SubscriptionResponse(
        id = id,
        tier = tier,
        isActive = isActive,
        startedAt = startedAt,
        expiresAt = expiresAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

}