package kr.co.wground.session.application

import kr.co.wground.global.common.UserId
import kr.co.wground.session.application.dto.SessionInfo

interface SessionQueryService {
    fun getActiveSessions(userId: UserId, currentSessionId: String): List<SessionInfo>
}
