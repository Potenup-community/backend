package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId

data class LeaderDto(
    val id: UserId,
    val name: String,
    val trackId: TrackId,
    val trackName: String,
    val profileImageUrl: String,
){
    companion object {
        fun from(dto: StudyQueryResult): LeaderDto {
            return LeaderDto(
                id = dto.leader.userId,
                name = dto.leader.name,
                trackId = dto.leader.trackId,
                trackName = dto.track.trackName,
                profileImageUrl = dto.leader.accessProfile()
            )
        }
    }
}
