package kr.co.wground.user.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.user.application.operations.UserService
import kr.co.wground.user.presentation.response.UserResponse
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService : UserService
){
    @GetMapping("/myInfo")
    fun getMyInfo(userId : CurrentUserId) : ResponseEntity<UserResponse> {
        val response = userService.getMyInfo(userId.value)
        return ResponseEntity.ok(response)
    }
}
