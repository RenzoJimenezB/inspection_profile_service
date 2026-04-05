package com.inspectpro.service

import com.inspectpro.model.CredentialStatus
import com.inspectpro.model.ProfileType
import com.inspectpro.model.Subscription
import com.inspectpro.model.SubscriptionTier
import com.inspectpro.repository.CredentialRepository
import com.inspectpro.repository.ProfileRepository
import com.inspectpro.repository.SubscriptionRepository
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant
import java.time.LocalDate

@Service
class SchedulerService(
    private val credentialRepository: CredentialRepository,
    private val profileRepository: ProfileRepository,
    private val subscriptionRepository: SubscriptionRepository,
) {
    private val logger = LoggerFactory.getLogger(SchedulerService::class.java)

    @Transactional
    fun processExpiredCredentials() {
        val today = LocalDate.now()
        val expiredCredentials = credentialRepository.findExpiredApprovedCredentials(today)

        if (expiredCredentials.isEmpty()) {
            logger.info("No expired credentials found")
        }

        logger.info("Processing ${expiredCredentials.size} expired credentials")
        expiredCredentials.forEach { credential ->
            credential.status = CredentialStatus.EXPIRED
            credentialRepository.save(credential)
            logger.info("Marked credential ${credential.id} as EXPIRED")

            checkAndDowngradeProfile(credential.profile.id)
        }
        notifyExpiringCredentials()
    }

    @Transactional
    fun processExpiredSubscriptions() {
        val now = Instant.now()
        val expiredSubscriptions = subscriptionRepository.findExpiredActiveSubscriptions(now)

        if (expiredSubscriptions.isEmpty()) {
            logger.info("No expired subscriptions found")
            return
        }

        logger.info("Processing ${expiredSubscriptions.size} expired subscriptions")

        expiredSubscriptions.forEach { subscription ->
            subscription.isActive = false
            subscriptionRepository.save(subscription)

            val user = subscription.user
            subscriptionRepository.save(
                Subscription(
                    user = user,
                    tier = SubscriptionTier.BASIC,
                    isActive = true,
                )
            )
            logger.info("Downgraded subscription for user ${user.id} to BASIC")
        }
    }

    private fun checkAndDowngradeProfile(profileId: Long) {
        val hasPendingCredential = credentialRepository.hasAnyPendingCredential(profileId)

        if (!hasPendingCredential) {
            logger.info("Profile $profileId has pending credentials, skipping downgrade")
            return
        }

        val approvedCount = credentialRepository.countByProfileIdAndStatus(
            profileId,
            CredentialStatus.APPROVED
        )

        if (approvedCount < 2) {
            val profile = profileRepository.findById(profileId)
                .orElse(null) ?: return

            if (profile.type == ProfileType.VERIFIED_PROFESSIONAL) {
                profile.type = ProfileType.BASIC
                profileRepository.save(profile)
                logger.info("Downgraded profile $profileId to BASIC")
            }
        }
    }

    private fun notifyExpiringCredentials() {
        val thirtyDaysFromNow = LocalDate.now().plusDays(30)
        val expiringCredentials = credentialRepository
            .findExpiredApprovedCredentials(thirtyDaysFromNow)
            .filter { it.expiryDate >= LocalDate.now() }

        expiringCredentials.forEach { credential ->
            logger.warn(
                "Credential ${credential.id} for profile ${credential.profile.id} expires on ${credential.expiryDate}"
            )
        }
    }
}