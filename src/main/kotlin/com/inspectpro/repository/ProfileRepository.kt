package com.inspectpro.repository

import com.inspectpro.model.Profile
import org.springframework.data.jpa.repository.JpaRepository

interface ProfileRepository : JpaRepository<Profile, Long> {
    fun findAllByUserIdAndIsActiveTrue(userId: Long): List<Profile>
    fun findByIdAndUserIdAndIsActiveTrue(id: Long, userId: Long): Profile?
}