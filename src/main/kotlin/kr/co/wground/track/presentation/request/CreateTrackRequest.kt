package kr.co.wground.track.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import kr.co.wground.track.presentation.request.validator.TrackDate
import kr.co.wground.track.presentation.request.validator.ValidTrackDate
import java.time.LocalDate

@ValidTrackDate
data class CreateTrackRequest(
    @field:NotBlank(message = "트랙 이름은 필수 입력사항입니다.")
    val trackName: String,
    @field:NotNull(message = "트랙 시작 일자는 필수 입력사항입니다.")
    override val startDate: LocalDate,
    @field:NotNull(message = "트랙 종료 일자는 필수 입력사항입니다.")
    override val endDate: LocalDate,
): TrackDate
