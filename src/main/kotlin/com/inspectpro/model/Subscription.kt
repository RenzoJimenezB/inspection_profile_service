package com.inspectpro.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.Instant

enum class SubscriptionTier {
    BASIC,
    ENHANCED,
    PROFESSIONAL
}

@Entity
@Table(name = "subscriptions")
class Subscription(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, columnDefinition = "subscription_tier")
    var tier: SubscriptionTier = SubscriptionTier.BASIC,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,

    @Column(name = "started_at", nullable = false)
    val startedAt: Instant = Instant.now(),

    @Column(name = "expires_at")
    var expiresAt: Instant? = null,
) : BaseEntity()