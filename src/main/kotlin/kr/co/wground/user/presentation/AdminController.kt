package kr.co.wground.user.presentation

import kr.co.wground.user.application.operations.AdminServiceImpl
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.AdminSearchUserResponse
import kr.co.wground.user.presentation.response.UserPageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PutMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminServiceImpl: AdminServiceImpl
) {
    @PutMapping("/users/decision")
    fun decisionSignUp(@RequestBody request: DecisionStatusRequest): ResponseEntity<Unit> {
        adminServiceImpl.decisionSignup(request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users/all")
    fun getAllUsers(
        @ModelAttribute condition: UserSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<UserPageResponse<AdminSearchUserResponse>> {
        val userInfos = adminServiceImpl.findUsersByConditions(condition, pageable)
        val response = UserPageResponse.fromAdminSearchUserResponse(userInfos)
        return ResponseEntity.ok(response)
    }
}
