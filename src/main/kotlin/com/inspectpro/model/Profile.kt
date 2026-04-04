package com.inspectpro.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table

enum class ProfileType {
    BASIC,
    VERIFIED_PROFESSIONAL
}

@Entity
@Table(name = "profiles")
class Profile(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    val user: User,

    @Column(name = "display_name", nullable = false, length = 100)
    var displayName: String,

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    var type: ProfileType = ProfileType.BASIC,

    @Column(name = "is_active", nullable = false)
    var isActive: Boolean = true,
) : BaseEntity()