package kr.co.wground.user.presentation

import kr.co.wground.user.application.Operations.AdminOperation
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.UserListResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminOperation: AdminOperation
) {
    @PutMapping("/users/decision")
    fun decisionSignUp(@RequestBody request: DecisionStatusRequest): ResponseEntity<Unit> {
        adminOperation.decisionSignup(request)
        return ResponseEntity.ok().build()
    }

    @GetMapping("/users/all")
    fun getAllUsers(
        @ModelAttribute condition: UserSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<UserListResponse>> {
        val responses = adminOperation.findUsersByConditions(condition, pageable)
        return ResponseEntity.ok(responses)
    }
}
