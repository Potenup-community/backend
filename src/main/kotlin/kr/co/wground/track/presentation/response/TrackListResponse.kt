package kr.co.wground.track.presentation.response

import io.swagger.v3.oas.annotations.media.ArraySchema
import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.application.dto.TrackQueryDto

@Schema(description = "과정 목록 응답")
data class TrackListResponse<T>(
    @field:ArraySchema(
        schema = Schema(implementation = TrackQueryDto::class),
        arraySchema = Schema(description = "과정 목록")
    )
    val content: List<T>,
)
