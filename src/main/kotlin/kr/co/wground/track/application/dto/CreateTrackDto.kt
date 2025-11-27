package kr.co.wground.track.application.dto

import java.time.LocalDate

data class CreateTrackDto(
    val trackName: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
)
