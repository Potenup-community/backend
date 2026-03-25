package kr.co.wground.gallery.application.usecase.command

import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import org.springframework.web.multipart.MultipartFile

data class UpdateProjectCommand(
    val projectId: ProjectId,
    val requesterId: UserId,
    val title: String? = null,
    val description: String? = null,
    val githubUrl: String? = null,
    val deployUrl: String? = null,
    val techStacks: List<String>? = null,
    val members: List<MemberAssignment>? = null,
    val thumbnailImage: MultipartFile? = null,
) {
    data class MemberAssignment(
        val userId: UserId,
        val position: Position,
    )
}
