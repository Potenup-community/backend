package kr.co.wground.user.controller

import kr.co.wground.user.controller.dto.request.DecisionStatusRequest
import kr.co.wground.user.service.requestsign.SignUpService
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