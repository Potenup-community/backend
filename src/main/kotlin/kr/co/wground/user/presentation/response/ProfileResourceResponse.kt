package kr.co.wground.user.presentation.response

import org.springframework.core.io.Resource


data class ProfileResourceResponse(
    val resource: Resource,
    val contentType: String
)
