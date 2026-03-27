package kr.co.wground.track.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive
import kr.co.wground.track.domain.constant.TrackType
import java.time.LocalDate

@Schema(description = "과정 생성 요청 데이터")
data class CreateTrackRequest(
    @field:Schema(
        description = "트랙 유형 (ADMIN 제외)",
        example = "FE",
        required = true
    )
    @field:NotNull(message = "트랙 유형은 필수 입력사항입니다.")
    val trackType: TrackType,

    @field:Schema(description = "기수(1 이상)", example = "3", required = false)
    @field:Positive(message = "기수는 1 이상의 정수여야 합니다.")
    val cardinal: Int? = null,

    @field:Schema(description = "시작 날짜", example = "2025-01-01")
    @field:NotNull(message = "트랙 시작 일자는 필수 입력사항입니다.")
    val startDate: LocalDate,

    @field:Schema(description = "종료 날짜", example = "2025-06-30")
    @field:NotNull(message = "트랙 종료 일자는 필수 입력사항입니다.")
    val endDate: LocalDate,
)
