package kr.co.wground.user.presentation

import kr.co.wground.user.application.requestsign.SignUpService
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val signUpService: SignUpService
) {

    @PutMapping("/users/decision")
    fun decisionSignUp(@RequestBody request: DecisionStatusRequest): ResponseEntity<Unit> {
        signUpService.decisionSignup(request)
        return ResponseEntity.ok().build()
    }
}
