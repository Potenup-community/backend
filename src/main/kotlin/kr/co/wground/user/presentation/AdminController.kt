package kr.co.wground.user.presentation

import kr.co.wground.user.application.operations.AdminServiceImpl
import kr.co.wground.user.infra.dto.UserInfoDto
import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.UserSearchRequest
import kr.co.wground.user.presentation.response.AdminSearchUserResponse
import org.springframework.data.domain.Page
import org.springframework.data.domain.Pageable
import org.springframework.data.web.PageableDefault
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/v1/admin")
class AdminController(
    private val adminServiceImpl: AdminServiceImpl
) {
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/users/decision")
    fun decisionSignUp(@RequestBody request: DecisionStatusRequest): ResponseEntity<Unit> {
        adminServiceImpl.decisionSignup(request)
        return ResponseEntity.noContent().build()
    }

    @GetMapping("/users/all")
    fun getAllUsers(
        @ModelAttribute condition: UserSearchRequest,
        @PageableDefault(size = 20) pageable: Pageable
    ): ResponseEntity<Page<AdminSearchUserResponse>> {
        val userInfos = adminServiceImpl.findUsersByConditions(condition, pageable)
        val responses = userInfoToResponse(userInfos)
        return ResponseEntity.ok(responses)
    }

    private fun userInfoToResponse(userInfos:  Page<UserInfoDto>): Page<AdminSearchUserResponse> {
        return userInfos.map { userInfoDto -> AdminSearchUserResponse(
            userId = userInfoDto.userId,
            name = userInfoDto.name,
            email = userInfoDto.email,
            phoneNumber = userInfoDto.phoneNumber,
            trackId = userInfoDto.trackId,
            role = userInfoDto.role,
            status = userInfoDto.status,
            requestStatus = userInfoDto.requestStatus,
            provider = userInfoDto.provider,
            createdAt = userInfoDto.createdAt,
        ) }
    }
}
