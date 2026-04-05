package com.inspectpro.controller

import com.inspectpro.dto.AuthResponse
import com.inspectpro.dto.ProfileResponse
import com.inspectpro.dto.UpdateProfileRequest
import com.inspectpro.security.AuthenticatedUser
import com.inspectpro.service.ProfileService
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/profiles")
class ProfileController(private val profileService: ProfileService) {

    @GetMapping("/me")
    fun getCurrentProfile(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<ProfileResponse> {
        return ResponseEntity.ok(profileService.getCurrentProfile(authenticatedUser))
    }

    @PutMapping("/me")
    fun updateCurrentProfile(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @Valid @RequestBody request: UpdateProfileRequest
    ): ResponseEntity<ProfileResponse> {
        return ResponseEntity.ok(profileService.updateCurrentProfile(authenticatedUser, request))
    }

    @GetMapping
    fun getAllProfiles(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser
    ): ResponseEntity<List<ProfileResponse>> {
        return ResponseEntity.ok(profileService.getAllProfiles(authenticatedUser))
    }

    @PostMapping("/switch/{profileId}")
    fun switchProfile(
        @AuthenticationPrincipal authenticatedUser: AuthenticatedUser,
        @PathVariable profileId: Long,
    ): ResponseEntity<AuthResponse> {
        return ResponseEntity.ok(profileService.switchProfile(authenticatedUser, profileId))
    }
}