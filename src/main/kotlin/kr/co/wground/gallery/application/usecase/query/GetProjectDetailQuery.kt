package kr.co.wground.gallery.application.usecase.query

import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId

data class GetProjectDetailQuery(
    val projectId: ProjectId,
    val userId: UserId,
)
