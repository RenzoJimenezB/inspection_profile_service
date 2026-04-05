package com.inspectpro.controller

import com.inspectpro.dto.CredentialRequest
import com.inspectpro.dto.CredentialResponse
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.service.CredentialService
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
class CredentialController(private val credentialService: CredentialService) {

    @PostMapping
    fun submitCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Valid @RequestBody request: CredentialRequest
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(credentialService.submitCredential(authenticatedUser, request))
    }

    @GetMapping
    fun getCredentials(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<List<CredentialResponse>> {
        return ResponseEntity.ok(credentialService.getCredentials(authenticatedUser))
    }

    @GetMapping("/{id}")
    fun getCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: Long
    ): ResponseEntity<CredentialResponse> {
        return ResponseEntity.ok(credentialService.getCredential(authenticatedUser, id))
    }

    @DeleteMapping("/{id}")
    fun deleteCredential(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable id: Long
    ): ResponseEntity<Unit> {
        credentialService.deleteCredential(authenticatedUser, id)
        return ResponseEntity.noContent().build()
    }
}
