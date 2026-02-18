package kr.co.wground.gallery.application.usecase.command

import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.UserId
import org.springframework.web.multipart.MultipartFile

data class CreateProjectCommand(
    val authorId: UserId,
    val title: String,
    val description: String,
    val githubUrl: String,
    val deployUrl: String?,
    val techStacks: List<String>,
    val members: List<MemberAssignment>,
    val thumbnailImage: MultipartFile,
) {
    data class MemberAssignment(val userId: UserId, val position: Position)
}
