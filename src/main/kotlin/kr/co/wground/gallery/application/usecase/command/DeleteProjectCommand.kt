package kr.co.wground.gallery.application.usecase.command

import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId

data class DeleteProjectCommand(
    val projectId: ProjectId,
    val requesterId: UserId,
)
