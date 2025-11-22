package kr.co.wground.user.controller

import kr.co.wground.user.controller.dto.request.SignUpRequest
import kr.co.wground.user.domain.User
import kr.co.wground.user.service.requestsign.SignUpService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/signup")
class RequestSignController(
    private val signUpService: SignUpService,
) {

    @PostMapping
    fun requestSignUp(@RequestBody requestSignup: SignUpRequest) : ResponseEntity<User> {
        signUpService.addRequestSignUp(requestSignup)
        return ResponseEntity.ok().build()
    }
}