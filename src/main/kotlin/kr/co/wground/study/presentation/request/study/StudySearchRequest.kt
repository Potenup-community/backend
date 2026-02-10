package kr.co.wground.study.presentation.request.study

import kr.co.wground.global.common.TrackId
import kr.co.wground.study.domain.enums.StudyStatus

data class StudySearchRequest(
    val trackId: TrackId? = null,
    val status: StudyStatus? = null,
)
