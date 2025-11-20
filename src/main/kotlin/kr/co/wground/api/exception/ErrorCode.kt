package kr.co.wground.api.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val message: String
    val httpStatus: HttpStatus
    val errorCode: String
}