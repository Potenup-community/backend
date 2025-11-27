package kr.co.wground.user.application.common

import kr.co.wground.user.presentation.request.SignUpRequest

interface SignUpService {
    fun addUser(request: SignUpRequest)
}
