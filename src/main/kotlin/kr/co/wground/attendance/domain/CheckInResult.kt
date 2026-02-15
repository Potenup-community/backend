package kr.co.wground.attendance.domain

sealed interface CheckInResult {
    data object CheckedIn : CheckInResult
    data object AlreadyCheckedIn : CheckInResult
}