package kr.co.wground.track.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.Positive
import kr.co.wground.track.domain.constant.TrackType
import java.time.LocalDate

@Schema(description = "과정 수정 요청 데이터")
data class UpdateTrackRequest(
    @field:Schema(description = "수정할 트랙 유형", example = "AI", required = false)
    val trackType: TrackType? = null,

    @field:Schema(description = "수정할 기수(1 이상). ADMIN인 경우 null", example = "5", required = false)
    @field:Positive(message = "기수는 1 이상의 정수여야 합니다.")
    val cardinal: Int? = null,

    @field:Schema(description = "수정할 시작 날짜", example = "2025-02-01", required = false)
    val startDate: LocalDate? = null,

    @field:Schema(description = "수정할 종료 날짜", example = "2025-07-30", required = false)
    val endDate: LocalDate? = null,
)
