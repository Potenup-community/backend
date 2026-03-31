package kr.co.wground.session.presentation

import kr.co.wground.global.jwt.UserPrincipal
import kr.co.wground.session.application.SessionCommandService
import kr.co.wground.session.application.SessionQueryService
import kr.co.wground.session.presentation.response.SessionListResponse
import kr.co.wground.session.presentation.response.SessionResponse
import org.springframework.http.ResponseEntity
import org.springframework.security.core.Authentication
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/v1/auth/sessions")
class SessionController(
    private val sessionQueryService: SessionQueryService,
    private val sessionCommandService: SessionCommandService,
) : SessionApi {

    @GetMapping
    override fun getSessions(authentication: Authentication): ResponseEntity<SessionListResponse> {
        val principal = authentication.principal as UserPrincipal
        val sessions = sessionQueryService.getActiveSessions(
            userId = principal.userId,
            currentSessionId = principal.sessionId ?: "",
        )
        return ResponseEntity.ok(SessionListResponse(sessions.map(SessionResponse::from)))
    }

    @DeleteMapping("/{sessionId}")
    override fun revokeSession(
        authentication: Authentication,
        @PathVariable sessionId: String,
    ): ResponseEntity<Unit> {
        val principal = authentication.principal as UserPrincipal
        sessionCommandService.revokeSession(principal.userId, sessionId)
        return ResponseEntity.noContent().build()
    }

    @DeleteMapping
    override fun revokeOtherSessions(authentication: Authentication): ResponseEntity<Unit> {
        val principal = authentication.principal as UserPrincipal
        sessionCommandService.revokeOtherSessions(
            userId = principal.userId,
            currentSessionId = principal.sessionId ?: "",
        )
        return ResponseEntity.noContent().build()
    }
}
