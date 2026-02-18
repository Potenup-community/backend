package kr.co.wground.study.application.dto

import com.querydsl.core.annotations.QueryProjection
import kr.co.wground.study.domain.Study
import kr.co.wground.track.domain.Track
import kr.co.wground.user.domain.User

data class StudyQueryResult @QueryProjection constructor(
    val study: Study,
    val leader: User,
    val track: Track
)
