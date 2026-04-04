package com.inspectpro.security

data class AuthenticatedUser(
    val userId: Long,
    val profileId: Long
)