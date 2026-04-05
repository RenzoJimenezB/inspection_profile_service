package com.inspectpro.scheduler

import com.inspectpro.service.SchedulerService
import org.slf4j.LoggerFactory
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
class CredentialExpirationScheduler(
    private val schedulerService: SchedulerService
) {
    private val logger = LoggerFactory.getLogger(CredentialExpirationScheduler::class.java)

    @Scheduled(cron = "0 0 2 * * *")
    fun processExpiredCredentials() {
        logger.info("Starting credential expiration job")
        schedulerService.processExpiredCredentials()
        schedulerService.processExpiredSubscriptions()
        logger.info("Completed credential expiration job")
    }
}