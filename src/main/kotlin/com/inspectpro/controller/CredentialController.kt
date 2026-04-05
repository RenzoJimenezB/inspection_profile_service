package com.inspectpro.controller

import com.inspectpro.dto.CredentialRequest
import com.inspectpro.dto.CredentialResponse
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.service.CredentialService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/credentials")
@Tag(
    name = "Credentials",
    description = "Professional credential management"
)
class CredentialController(private val credentialService: CredentialService) {

    @Operation(
        summary = "Submit credential",
        description = "Submits a new professional credential for review"
    )
    @PostMapping
    fun submitCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Valid @RequestBody request: CredentialRequest
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(credentialService.submitCredential(authenticatedUser, request))
    }

    @Operation(
        summary = "List credentials",
        description = "Returns all credentials for the current profile"
    )
    @GetMapping
    fun getCredentials(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<List<CredentialResponse>> {
        return ResponseEntity.ok(credentialService.getCredentials(authenticatedUser))
    }

    @Operation(
        summary = "Get credential",
        description = "Returns a specific credential by ID"
    )
    @GetMapping("/{id}")
    fun getCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: Long
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.ok(credentialService.getCredential(authenticatedUser, id))
    }

    @Operation(
        summary = "Delete credential",
        description = "Deletes a PENDING or REJECTED credential"
    )
    @DeleteMapping("/{id}")
    fun deleteCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        credentialService.deleteCredential(authenticatedUser, id)
        return ResponseEntity.noContent().build()
    }
}
