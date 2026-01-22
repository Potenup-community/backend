package kr.co.wground.notification.domain.enums

enum class ReferenceType(val basePath: String) {
    POST("/posts"),
    COMMENT("/comments"),
    STUDY("/studies"),
}
