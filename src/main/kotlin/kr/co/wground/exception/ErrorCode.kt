package kr.co.wground.exception

import org.springframework.http.HttpStatus

interface ErrorCode {
    val message: String
    val httpStatus: HttpStatus
    val errorCode: String
}
