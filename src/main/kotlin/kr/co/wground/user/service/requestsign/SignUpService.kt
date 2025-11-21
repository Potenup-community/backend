package kr.co.wground.user.service.requestsign

import kr.co.wground.user.domain.RequestSignup

interface SignUpService {
    fun addRequestSignUp(requestSignup: RequestSignup)
}