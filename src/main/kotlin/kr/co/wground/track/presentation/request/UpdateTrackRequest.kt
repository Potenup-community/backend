package kr.co.wground.track.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import kr.co.wground.track.presentation.request.validator.TrackDate
import kr.co.wground.track.presentation.request.validator.ValidTrackDate
import java.time.LocalDate

@ValidTrackDate
data class UpdateTrackRequest(
    @field:NotBlank(message = "이름은 공백일 수 없습니다.")
    @field:Size(min = 1, max = 50, message = "이름은 1~50자 사이여야 합니다.")
    val trackName: String? = null,
    override val startDate: LocalDate? = null,
    override val endDate: LocalDate? = null,
) : TrackDate
