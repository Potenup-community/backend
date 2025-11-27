package kr.co.wground.track.presentation.request.validator

import java.time.LocalDate

interface TrackDate {
    val startDate: LocalDate?
    val endDate: LocalDate?
}