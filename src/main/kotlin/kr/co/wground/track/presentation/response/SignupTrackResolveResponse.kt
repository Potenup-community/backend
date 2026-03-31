package kr.co.wground.track.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.global.common.TrackId
import kr.co.wground.track.application.dto.TrackResolveDto
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "회원가입용 트랙 식별자 조회 응답")
data class SignupTrackResolveResponse(
    @field:Schema(description = "트랙 유형", example = "FE")
    val trackType: TrackType,
    @field:Schema(description = "기수", example = "3")
    val cardinal: Int,
    @field:Schema(description = "선택된 trackId", example = "12")
    val trackId: TrackId,
) {
    companion object {
        fun from(dto: TrackResolveDto): SignupTrackResolveResponse {
            return SignupTrackResolveResponse(
                trackType = dto.trackType,
                cardinal = dto.cardinal,
                trackId = dto.trackId
            )
        }
    }
}
