package kr.co.wground.user.controller

import kr.co.wground.user.controller.dto.request.SignUpRequest
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/signup")
class RequestSignController {

    @PostMapping
    fun requestSignUp(@RequestBody requestSignup: SignUpRequest) : ResponseEntity<Unit> {

        return ResponseEntity.ok().build()
    }
}