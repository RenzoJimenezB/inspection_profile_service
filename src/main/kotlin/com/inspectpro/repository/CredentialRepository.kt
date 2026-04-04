package com.inspectpro.repository

import com.inspectpro.model.Credential
import com.inspectpro.model.CredentialStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.time.LocalDate

interface CredentialRepository : JpaRepository<Credential, Long> {
    fun findAllByProfileId(profileId: Long): List<Credential>
    fun findByIdAndProfileId(id: Long, profileId: Long): Credential?
    fun countByProfileIdAndStatus(profileId: Long, status: CredentialStatus): Long

    @Query(
        """
        SELECT c FROM Credential c
        WHERE c.expiryDate <= :date
        AND c.status = 'APPROVED'
    """
    )
    fun findExpiredApprovedCredentials(date: LocalDate): List<Credential>

    @Query(
        """
        SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END
        FROM Credential c
        WHERE c.profile.id = :profileId
        AND c.status = 'PENDING'
    """
    )
    fun hasAnyPendingCredential(profileId: Long): Boolean
}