package com.inspectpro.controller

import com.inspectpro.dto.CredentialResponse
import com.inspectpro.dto.UpdateCredentialStatusRequest
import com.inspectpro.service.CredentialService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
class AdminController(
    private val credentialService: CredentialService,
    private val schedulerService: SchedulerService
) {

    @PutMapping("/credentials/{id}/status")
    fun updateCredentialStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCredentialStatusRequest
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.ok(
            credentialService.updateCredentialStatus(id, request.status)
        )
    }

    @PostMapping("/jobs/credential-expiration")
    fun triggerCredentialExpiration(): ResponseEntity<Unit> {
        schedulerService.processExpiredCredentials()
        return ResponseEntity.ok().build()
    }
}