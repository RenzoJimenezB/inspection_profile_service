package com.inspectpro.repository

import com.inspectpro.model.Subscription
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.Instant

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByUserIdAndIsActiveTrue(userId: Long): Subscription?

    @Query(
        """
        SELECT s FROM Subscription s
        WHERE s.expiresAt <= :now
        AND s.isActive = true
    """
    )
    fun findExpiredActiveSubscriptions(now: Instant): List<Subscription>
}