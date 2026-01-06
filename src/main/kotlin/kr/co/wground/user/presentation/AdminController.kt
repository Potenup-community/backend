package kr.co.wground.user.presentation

import kr.co.wground.global.common.UserId
import kr.co.wground.user.application.operations.AdminServiceImpl
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.application.operations.dto.AdminSearchUserDto
import kr.co.wground.user.application.operations.dto.ConditionDto
import kr.co.wground.user.application.operations.dto.DecisionDto
import kr.co.wground.user.presentation.request.MultipleDecisionRequest
import kr.co.wground.user.presentation.response.UserPageResponse
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ModelAttribute
import org.springframework.web.bind.annotation.PathVariable
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
    @PutMapping("/users/{userId}/decision")
    fun decisionSignUp(
        @PathVariable userId: UserId,
        @RequestBody request: DecisionStatusRequest
    ): ResponseEntity<Unit> {
        adminServiceImpl.decisionSignup(DecisionDto.single(userId,request))
        return ResponseEntity.noContent().build()
    }

    @PutMapping("/users/decisions")
    fun multipleDecision(@RequestBody request: MultipleDecisionRequest): ResponseEntity<Unit> {
        adminServiceImpl.decisionSignup(DecisionDto.from(request))
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users")
    fun getAllUsers(
        @ModelAttribute condition: UserSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<UserPageResponse<AdminSearchUserDto>> {
        val userInfos = adminServiceImpl.findUsersByConditions(ConditionDto.from(condition), pageable)
        val response = UserPageResponse.fromAdminSearchUserDto(userInfos)
        return ResponseEntity.ok(response)
    }
}
