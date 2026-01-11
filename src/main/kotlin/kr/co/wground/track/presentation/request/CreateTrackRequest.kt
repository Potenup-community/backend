package kr.co.wground.track.presentation.request

import io.swagger.v3.oas.annotations.media.Schema
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.LocalDate

@Schema(description = "과정 생성 요청 데이터")
data class CreateTrackRequest(
    @field:Schema(description = "과정 이름", example = "Backend 1기")
    @field:NotBlank(message = "트랙 이름은 필수 입력사항입니다.")
    val trackName: String,

    @field:Schema(description = "시작 날짜", example = "2025-01-01")
    @field:NotNull(message = "트랙 시작 일자는 필수 입력사항입니다.")
    val startDate: LocalDate,

    @field:Schema(description = "종료 날짜", example = "2025-06-30")
    @field:NotNull(message = "트랙 종료 일자는 필수 입력사항입니다.")
    val endDate: LocalDate,
)
