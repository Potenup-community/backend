package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.enums.StudyStatus
import kr.co.wground.track.domain.constant.TrackType

data class StudySearchCondition(
    val trackId: TrackId? = null,
    val trackType: TrackType? = null,
    val cardinal: Int? = null,
    val status: StudyStatus? = null,
)
