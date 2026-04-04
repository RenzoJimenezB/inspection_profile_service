package com.inspectpro.security

import com.inspectpro.config.JwtProperties
import io.jsonwebtoken.Claims
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.security.Keys
import org.springframework.stereotype.Service
import java.util.Date

@Service
class JwtService(private val jwtProperties: JwtProperties) {

    private val signingKey by lazy {
        Keys.hmacShaKeyFor(jwtProperties.secret.toByteArray())
    }

    fun generateAccessToken(userId: Long, profileId: Long, role: String): String {
        return Jwts.builder()
            .subject(userId.toString())
            .claim("profileId", profileId)
            .claim("role", role)
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.expiration))
            .signWith(signingKey)
            .compact()
    }

    fun generateRefreshToken(userId: Long): String {
        return Jwts.builder()
            .subject(userId.toString())
            .issuedAt(Date())
            .expiration(Date(System.currentTimeMillis() + jwtProperties.refreshExpiration))
            .signWith(signingKey)
            .compact()
    }

    fun validateToken(token: String): Boolean {
        return try {
            getClaims(token)
            true
        } catch (e: Exception) {
            false
        }
    }

    fun getUserIdFromToken(token: String): Long {
        return getClaims(token).subject.toLong()
    }

    fun getProfileIdFromToken(token: String): Long {
        return getClaims(token)["profileId"].toString().toLong()
    }

    fun getRoleFromToken(token: String): String {
        return getClaims(token)["role"].toString()
    }

    private fun getClaims(token: String): Claims {
        return Jwts.parser()
            .verifyWith(signingKey)
            .build()
            .parseSignedClaims(token)
            .payload
    }
}