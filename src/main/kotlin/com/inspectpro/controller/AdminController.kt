package com.inspectpro.controller

import com.inspectpro.dto.CredentialResponse
import com.inspectpro.dto.UpdateCredentialStatusRequest
import com.inspectpro.service.CredentialService
import com.inspectpro.service.SchedulerService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
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
@Tag(
    name = "Admin",
    description = "Administrative endpoints for credential management and job triggers"
)
class AdminController(
    private val credentialService: CredentialService,
    private val schedulerService: SchedulerService
) {

    @Operation(
        summary = "Update credential status",
        description = "Approves or rejects a pending credential. Triggers VP promotion if applicable"
    )
    @PutMapping("/credentials/{id}/status")
    fun updateCredentialStatus(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateCredentialStatusRequest
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.ok(
            credentialService.updateCredentialStatus(id, request.status)
        )
    }

    @Operation(
        summary = "Trigger credential expiration job",
        description = "Manually triggers the credential expiration job and downgrades profiles as needed"
    )
    @PostMapping("/jobs/credential-expiration")
    fun triggerCredentialExpiration(): ResponseEntity<Unit> {
        schedulerService.processExpiredCredentials()
        return ResponseEntity.ok().build()
    }
}