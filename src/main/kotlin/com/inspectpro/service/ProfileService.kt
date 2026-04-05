package com.inspectpro.service

import com.inspectpro.dto.AuthResponse
import com.inspectpro.dto.ProfileResponse
import com.inspectpro.dto.UpdateProfileRequest
import com.inspectpro.exception.ForbiddenException
import com.inspectpro.exception.ResourceNotFoundException
import com.inspectpro.model.Profile
import com.inspectpro.model.RefreshToken
import com.inspectpro.repository.ProfileRepository
import com.inspectpro.repository.RefreshTokenRepository
import com.inspectpro.repository.UserRepository
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.security.JwtService
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class ProfileService(
    private val profileRepository: ProfileRepository,
    private val userRepository: UserRepository,
    private val jwtService: JwtService,
    private val refreshTokenRepository: RefreshTokenRepository
) {

    fun getCurrentProfile(authenticatedUser: AuthenticatedUser): ProfileResponse {
        val profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(
            authenticatedUser.profileId,
            authenticatedUser.userId
        ) ?: throw ResourceNotFoundException("Profile not found")
        return profile.toResponse()
    }

    @Transactional
    fun updateCurrentProfile(
        authenticatedUser: AuthenticatedUser,
        request: UpdateProfileRequest
    ): ProfileResponse {
        val profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(
            authenticatedUser.profileId,
            authenticatedUser.userId
        ) ?: throw ResourceNotFoundException("Profile not found")

        profile.displayName = request.displayName
        return profileRepository.save(profile).toResponse()
    }

    fun getAllProfiles(authenticatedUser: AuthenticatedUser): List<ProfileResponse> {
        return profileRepository
            .findAllByUserIdAndIsActiveTrue(authenticatedUser.userId)
            .map { it.toResponse() }
    }

    @Transactional
    fun switchProfile(
        authenticatedUser: AuthenticatedUser,
        profileId: Long
    ): AuthResponse {
        val profile = profileRepository.findByIdAndUserIdAndIsActiveTrue(
            profileId,
            authenticatedUser.userId
        ) ?: throw ForbiddenException("Profile not found or does not belong to user")

        val user = userRepository
            .findById(authenticatedUser.userId)
            .orElseThrow { ResourceNotFoundException("User not found") }

        refreshTokenRepository.deleteByUserId(user.id)

        val accessToken = jwtService.generateAccessToken(
            userId = user.id,
            profileId = profile.id,
            role = user.role.name
        )

        val refreshToken = jwtService.generateRefreshToken(user.id)
        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = refreshToken,
                expiresAt = Instant.now().plusMillis(jwtService.getRefreshExpiration()),
            )
        )

        return AuthResponse(accessToken, refreshToken)
    }

    private fun Profile.toResponse() = ProfileResponse(
        id = id,
        displayName = displayName,
        type = type,
        isActive = isActive,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )
}