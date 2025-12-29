package kr.co.wground.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val httpStatus: HttpStatus
    val code: String
    val message: String
}
