package com.inspectpro.service

import com.inspectpro.dto.AuthRequest
import com.inspectpro.dto.AuthResponse
import com.inspectpro.dto.RefreshRequest
import com.inspectpro.dto.RefreshResponse
import com.inspectpro.exception.BusinessException
import com.inspectpro.exception.DuplicateResourceException
import com.inspectpro.exception.ResourceNotFoundException
import com.inspectpro.model.Profile
import com.inspectpro.model.RefreshToken
import com.inspectpro.model.Subscription
import com.inspectpro.model.User
import com.inspectpro.model.UserRole
import com.inspectpro.repository.ProfileRepository
import com.inspectpro.repository.RefreshTokenRepository
import com.inspectpro.repository.SubscriptionRepository
import com.inspectpro.repository.UserRepository
import com.inspectpro.security.JwtService
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class AuthService(
    private val userRepository: UserRepository,
    private val profileRepository: ProfileRepository,
    private val subscriptionRepository: SubscriptionRepository,
    private val refreshTokenRepository: RefreshTokenRepository,
    private val jwtService: JwtService,
    private val passwordEncoder: PasswordEncoder,
) {

    @Transactional
    fun register(request: AuthRequest): AuthResponse {
        if (userRepository.existsByEmail(request.email)) {
            throw DuplicateResourceException("Email already registered")
        }

        val user = userRepository.save(
            User(
                email = request.email,
                password = passwordEncoder.encode(request.password),
                role = UserRole.USER,
            )
        )

        val profile = profileRepository.save(
            Profile(
                user = user,
                displayName = request.email.substringBefore("@")
            )
        )

        subscriptionRepository.save(
            Subscription(user = user)
        )

        return generateAuthResponse(user, profile.id)
    }

    @Transactional
    fun login(request: AuthRequest): AuthResponse {
        val user = userRepository.findByEmail(request.email)
            ?: throw BusinessException("Invalid credentials")

        if (!passwordEncoder.matches(
                request.password,
                user.password
            )
        ) {
            throw BusinessException("Invalid credentials")
        }

        val profile = profileRepository
            .findAllByUserIdAndIsActiveTrue(user.id)
            .firstOrNull() ?: throw ResourceNotFoundException("No active profile found")

        return generateAuthResponse(user, profile.id)
    }

    @Transactional
    fun refresh(request: RefreshRequest): RefreshResponse {
        val refreshToken = refreshTokenRepository.findByToken(request.refreshToken)
            ?: throw BusinessException("Invalid refresh token")

        if (refreshToken.expiresAt.isBefore(Instant.now())) {
            refreshTokenRepository.delete(refreshToken)
            throw BusinessException("Refresh token expired")
        }

        refreshTokenRepository.delete(refreshToken)

        val user = refreshToken.user
        val profile = profileRepository.findAllByUserIdAndIsActiveTrue(user.id)
            .firstOrNull() ?: throw ResourceNotFoundException("No active profile found")

        val newAccessToken = jwtService.generateAccessToken(
            userId = user.id,
            profileId = profile.id,
            role = user.role.name
        )

        val newRefreshToken = saveRefreshToken(user)

        return RefreshResponse(
            accessToken = newAccessToken,
            refreshToken = newRefreshToken,
        )
    }

    private fun generateAuthResponse(user: User, profileId: Long): AuthResponse {
        val accessToken = jwtService.generateAccessToken(
            userId = user.id,
            profileId = profileId,
            role = user.role.name
        )
        val refreshToken = saveRefreshToken(user)
        return AuthResponse(accessToken, refreshToken)
    }

    private fun saveRefreshToken(user: User): String {
        val token = jwtService.generateRefreshToken(user.id)
        refreshTokenRepository.save(
            RefreshToken(
                user = user,
                token = token,
                expiresAt = Instant.now().plusMillis(jwtService.getRefreshExpiration()),
            )
        )
        return token
    }
}
