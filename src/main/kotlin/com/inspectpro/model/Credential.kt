package com.inspectpro.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import java.time.LocalDate

enum class CredentialType {
    HVAC_LICENSE,
    EPA_CERTIFICATION,
    INSURANCE,
    STATE_LICENSE
}

enum class CredentialStatus {
    PENDING,
    APPROVED,
    REJECTED,
    EXPIRED
}

@Entity
@Table(name = "credentials")
class Credential(
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "profile_id", nullable = false)
    val profile: Profile,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    val type: CredentialType,

    @Column(nullable = false)
    val issuer: String,

    @Column(name = "license_number", nullable = false)
    val licenseNumber: String,

    @Column(name = "expiry_date", nullable = false)
    val expiryDate: LocalDate,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    var status: CredentialStatus = CredentialStatus.PENDING,
) : BaseEntity()