package kr.co.wground.point.presentation

import kr.co.wground.global.config.resolver.CurrentUserId
import kr.co.wground.point.application.command.usecase.AdminPointUseCase
import kr.co.wground.point.presentation.request.AdminGivePointRequest
import org.springframework.http.ResponseEntity
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@PreAuthorize("hasRole('ADMIN')")
@RequestMapping("/api/v1/admin/points")
class AdminPointController(
    private val adminPointUseCase: AdminPointUseCase,
) : AdminPointApi {

    @PostMapping("/give")
    override fun givePoint(
        adminId: CurrentUserId,
        @RequestBody request: AdminGivePointRequest,
    ): ResponseEntity<Unit> {
        adminPointUseCase.givePoint(request.userId, request.amount, adminId.value)
        return ResponseEntity.noContent().build()
    }
}