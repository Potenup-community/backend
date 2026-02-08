package kr.co.wground.study.application.dto

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.enums.StudyStatus

data class StudySearchCondition(
    val trackId: TrackId? = null,
    val status: StudyStatus? = null,
)
