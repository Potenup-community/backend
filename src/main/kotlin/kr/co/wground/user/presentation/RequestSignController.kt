package kr.co.wground.user.presentation

import jakarta.validation.Valid
import kr.co.wground.user.application.common.SignUpService
import kr.co.wground.user.presentation.request.SignUpRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class RequestSignController(
    private val signUpService: SignUpService,
) {
    @PostMapping("/signup")
    fun requestSignUp(@Valid @RequestBody requestSignup: SignUpRequest) : ResponseEntity<Unit> {
        signUpService.addUser(requestSignup)
        return ResponseEntity.ok().build()
    }
}
