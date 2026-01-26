package kr.co.wground.track.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size
import java.time.LocalDate

@Schema(description = "과정 수정 요청 데이터")
data class UpdateTrackRequest(
    @field:Schema(description = "수정할 과정 이름", example = "Backend 1기 (수정)", required = false)
    @field:Size(max = 50, message = "이름은 50자를 넘길 수 없습니다.")
    @field:Pattern(
        regexp = ".*\\S.*",
        message = "이름은 공백으로만 구성될 수 없습니다."
    )
    val trackName: String? = null,

    @field:Schema(description = "수정할 시작 날짜", example = "2025-02-01", required = false)
    val startDate: LocalDate? = null,

    @field:Schema(description = "수정할 종료 날짜", example = "2025-07-30", required = false)
    val endDate: LocalDate? = null,
)
