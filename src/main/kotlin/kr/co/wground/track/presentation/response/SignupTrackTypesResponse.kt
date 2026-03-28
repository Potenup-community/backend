package kr.co.wground.track.presentation.response

import io.swagger.v3.oas.annotations.media.Schema
import kr.co.wground.track.domain.constant.TrackType

@Schema(description = "회원가입용 트랙 유형 목록 응답")
data class SignupTrackTypesResponse(
    val trackTypes: List<TrackTypeItem>,
) {
    data class TrackTypeItem(
        @field:Schema(description = "트랙 유형 코드", example = "FE")
        val trackType: TrackType,
        @field:Schema(description = "트랙 유형 표시명", example = "FE")
        val label: String,
        @field:Schema(description = "기수 입력 필요 여부", example = "true")
        val requiresCardinal: Boolean,
    )

    companion object {
        fun from(trackTypes: List<TrackType>): SignupTrackTypesResponse {
            return SignupTrackTypesResponse(
                trackTypes = trackTypes.map {
                    TrackTypeItem(
                        trackType = it,
                        label = it.displayName,
                        requiresCardinal = true
                    )
                }
            )
        }
    }
}
