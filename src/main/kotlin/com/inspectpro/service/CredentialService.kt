package com.inspectpro.service

import com.inspectpro.dto.CredentialRequest
import com.inspectpro.dto.CredentialResponse
import com.inspectpro.exception.BusinessException
import com.inspectpro.exception.ResourceNotFoundException
import com.inspectpro.model.Credential
import com.inspectpro.model.CredentialStatus
import com.inspectpro.model.ProfileType
import com.inspectpro.repository.CredentialRepository
import com.inspectpro.repository.ProfileRepository
import com.inspectpro.security.AuthenticatedUser
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDate

@Service
class CredentialService(
    private val credentialRepository: CredentialRepository,
    private val profileRepository: ProfileRepository,
) {

    @Transactional
    fun submitCredential(
        authenticatedUser: AuthenticatedUser,
        request: CredentialRequest,
    ): CredentialResponse {
        val profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(
            authenticatedUser.profileId,
            authenticatedUser.userId
        ) ?: throw ResourceNotFoundException("Profile not found")

        if (request.expiryDate.isBefore(LocalDate.now())) {
            throw BusinessException("Expiry date must be in the future")
        }

        val credential = credentialRepository.save(
            Credential(
                profile = profile,
                type = request.type,
                issuer = request.issuer,
                licenseNumber = request.licenseNumber,
                expiryDate = request.expiryDate,
                status = CredentialStatus.PENDING,
            )
        )
        return credential.toResponse()
    }

    fun getCredentials(authenticatedUser: AuthenticatedUser): List<CredentialResponse> {
        return credentialRepository.findAllByProfileId(authenticatedUser.profileId)
            .map { it.toResponse() }
    }

    fun getCredential(
        authenticatedUser: AuthenticatedUser,
        credentialId: Long
    ): CredentialResponse {
        val credential = credentialRepository.findByIdAndProfileId(
            credentialId,
            authenticatedUser.profileId
        ) ?: throw ResourceNotFoundException("Credential not found")

        return credential.toResponse()
    }

    @Transactional
    fun deleteCredential(
        authenticatedUser: AuthenticatedUser,
        credentialId: Long,
    ) {
        val credential = credentialRepository.findByIdAndProfileId(
            credentialId,
            authenticatedUser.profileId
        ) ?: throw ResourceNotFoundException("Credential not found")

        if (credential.status != CredentialStatus.PENDING &&
            credential.status != CredentialStatus.REJECTED
        ) {
            throw BusinessException("Only PENDING and REJECTED credentials can be deleted")
        }
        credentialRepository.delete(credential)
    }

    @Transactional
    fun updateCredentialStatus(credentialId: Long, status: CredentialStatus): CredentialResponse {
        val credential = credentialRepository.findById(credentialId)
            .orElseThrow { ResourceNotFoundException("Credential not found") }

        if (credential.status != CredentialStatus.PENDING) {
            throw BusinessException("Only PENDING can be approved or rejected")
        }

        if (status == CredentialStatus.APPROVED &&
            credential.expiryDate.isBefore(LocalDate.now())
        ) {
            throw BusinessException("Cannot approve an expired credential")
        }

        if (status != CredentialStatus.APPROVED && status != CredentialStatus.REJECTED) {
            throw BusinessException("Status must be APPROVED or REJECTED")
        }

        credential.status = status
        credentialRepository.save(credential)

        if (status == CredentialStatus.APPROVED) {
            promoteProfileEligible(credential.profile.id)
        }
        return credential.toResponse()
    }

    private fun promoteProfileEligible(profileId: Long) {
        val approvedCount = credentialRepository.countByProfileIdAndStatus(
            profileId,
            CredentialStatus.APPROVED
        )

        if (approvedCount >= 2) {
            val profile = profileRepository.findById(profileId)
                .orElseThrow { ResourceNotFoundException("Profile not found") }
            profile.type = ProfileType.VERIFIED_PROFESSIONAL
            profileRepository.save(profile)
        }
    }

    private fun Credential.toResponse() = CredentialResponse(
        id = id,
        profileId = profile.id,
        type = type,
        issuer = issuer,
        licenseNumber = licenseNumber,
        expiryDate = expiryDate,
        status = status,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}