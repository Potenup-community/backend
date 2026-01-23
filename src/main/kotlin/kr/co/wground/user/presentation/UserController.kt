package kr.co.wground.user.presentation

import jakarta.validation.Valid
import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.user.application.common.SignUpService
import kr.co.wground.user.application.operations.UserService
import kr.co.wground.user.presentation.request.SignUpRequest
import kr.co.wground.user.presentation.response.UserResponse
import kr.co.wground.user.presentation.response.UserSummaryResponse
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userService: UserService,
    private val signUpService: SignUpService,
) : UserApi {
    @GetMapping("/myInfo")
    override fun getMyInfo(userId: CurrentUserId): ResponseEntity<UserResponse> {
        val response = userService.getMyInfo(userId.value)
        return ResponseEntity.ok(UserResponse.from(response))
    }

    @PostMapping("/signup")
    override fun requestSignUp(@Valid @RequestBody requestSignup: SignUpRequest): ResponseEntity<Unit> {
        signUpService.addUser(requestSignup)
        return ResponseEntity.status(HttpStatus.CREATED).build()
    }

    @GetMapping
    override fun getUsersForMention(
        @RequestParam(defaultValue = "20") size: Int,
        @RequestParam(required = false) cursorId: Long?
    ): ResponseEntity<List<UserSummaryResponse>> {
        val users = userService.getUsersForMention(size, cursorId)
        return ResponseEntity.ok(users.map { UserSummaryResponse.from(it) })
    }
}
