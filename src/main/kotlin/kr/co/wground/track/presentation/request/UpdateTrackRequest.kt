package kr.co.wground.track.presentation.request

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

data class UpdateTrackRequest(
    @field:Size(max = 50, message = "이름은 50자를 넘길 수 없습니다.")
    @field:Pattern(
        regexp = ".*\\S.*",
        message = "이름은 공백으로만 구성될 수 없습니다."
    )
    val trackName: String? = null,
    val startDate: LocalDate? = null,
    val endDate: LocalDate? = null,
)
