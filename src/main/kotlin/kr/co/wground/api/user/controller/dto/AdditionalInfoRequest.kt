package kr.co.wground.api.user.controller.dto

data class AdditionalInfoRequest(
    val phoneNumber: String,
    val affiliationId: Long
)