package kr.co.wground.user.presentation

import kr.co.wground.user.application.login.LoginService
import kr.co.wground.user.presentation.request.LoginRequest
import kr.co.wground.user.presentation.response.LoginResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth")
class AuthController(private val memberService : LoginService) {

    @PostMapping
    fun login(@RequestBody loginRequest: LoginRequest): ResponseEntity<LoginResponse> {
        val response = memberService.login(loginRequest)
        return ResponseEntity.ok(response)
    }
}