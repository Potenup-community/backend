package kr.co.wground.track.presentation.response

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.application.dto.TrackCardinalsDto
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "트랙 유형별 등록 기수 목록 응답")
data class TrackCardinalsResponse(
    @field:Schema(description = "트랙 유형", example = "FE")
    val trackType: TrackType,
    @field:ArraySchema(arraySchema = Schema(description = "등록된 기수 목록", example = "[1,2,4]"))
    val cardinals: List<Int>,
) {
    companion object {
        fun from(dto: TrackCardinalsDto): TrackCardinalsResponse {
            return TrackCardinalsResponse(
                trackType = dto.trackType,
                cardinals = dto.cardinals
            )
        }
    }
}
