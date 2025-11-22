package kr.co.wground.user.service.requestsign

import kr.co.wground.user.controller.dto.request.SignUpRequest

interface SignUpService {
    fun addRequestSignUp(requestSignup: SignUpRequest)
}