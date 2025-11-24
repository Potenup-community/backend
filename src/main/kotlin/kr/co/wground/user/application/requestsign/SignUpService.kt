package kr.co.wground.user.application.requestsign

import kr.co.wground.user.presentation.request.DecisionStatusRequest
import kr.co.wground.user.presentation.request.SignUpRequest

interface SignUpService {
    fun addUser(request: SignUpRequest)
    fun decisionSignup(request: DecisionStatusRequest)
}
