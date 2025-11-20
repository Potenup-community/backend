package kr.co.wground.api.user.controller

import kr.co.wground.api.user.controller.dto.AdditionalInfoRequest
import kr.co.wground.api.user.domain.RequestSignup
import kr.co.wground.api.user.domain.constant.UserSignupStatus
import kr.co.wground.api.user.service.UserService
import org.springframework.http.ResponseEntity
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

class UserController {
    @RestController
    @RequestMapping("/api/v1/users")
    class UserController(
        private val userService: UserService
    ) {

        @PostMapping("/me/signup-info")
        fun submitAdditionalInfo(
            principal: OAuth2AuthenticationToken,
            @RequestBody request: AdditionalInfoRequest
        ): ResponseEntity<Void> {
            userService.saveAdditionalInfo(principal.principal.name, request)
            return ResponseEntity.accepted().build()
        }

        @GetMapping("/admin/signups")
        fun listPending(): List<RequestSignup> = userService.listPending()

        @PostMapping("/admin/signups/{id}/approve")
        fun approve(
            @PathVariable id: Long,
            @RequestParam status: UserSignupStatus
        ) = userService.approve(id, status)

        @PostMapping("/admin/signups/{id}/reject")
        fun reject(@PathVariable id: Long) = userService.reject(id)
    }
}