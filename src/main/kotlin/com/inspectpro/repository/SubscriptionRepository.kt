package com.inspectpro.repository

import com.inspectpro.model.Subscription
import org.springframework.data.jpa.repository.JpaRepository

interface SubscriptionRepository : JpaRepository<Subscription, Long> {
    fun findByUserIdAndIsActiveTrue(userId: Long): Subscription?
}