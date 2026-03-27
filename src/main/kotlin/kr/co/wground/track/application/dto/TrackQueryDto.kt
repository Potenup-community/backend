package kr.co.wground.track.application.dto

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.domain.Track
import kr.co.wground.track.domain.constant.TrackStatus
import kr.co.wground.track.domain.constant.TrackType
import java.time.LocalDate

@Schema(description = "과정 조회 응답 DTO")
data class TrackQueryDto private constructor(
    @field:Schema(description = "과정 ID", example = "2")
    val trackId: Long,
    @field:Schema(description = "과정 이름(응답 표준 필드)", example = "FE 1기")
    val trackName: String,
    @field:Schema(hidden = true, description = "내부 전환용 필드")
    val trackType: TrackType?,
    @field:Schema(hidden = true, description = "내부 전환용 필드")
    val cardinal: Int?,
    @field:Schema(hidden = true, description = "내부 전환용 필드")
    val displayName: String,
    @field:Schema(description = "시작 날짜", example = "2026-01-01")
    val startDate: LocalDate,
    @field:Schema(description = "종료 날짜", example = "2026-06-30")
    val endDate: LocalDate,
    @field:Schema(description = "과정 상태", example = "ENROLLED")
    val trackStatus: TrackStatus
) {
    companion object {
        fun Track.toTrackQueryDto(): TrackQueryDto {
            return TrackQueryDto(
                trackId = this.trackId,
                trackName = this.displayName(),
                trackType = this.trackType,
                cardinal = this.cardinal,
                displayName = this.displayName(),
                startDate = this.startDate,
                endDate = this.endDate,
                trackStatus = this.trackStatus
            )
        }
    }
}
