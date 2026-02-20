package kr.co.wground.gallery.presentation.request

import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import kr.co.wground.gallery.application.usecase.command.UpdateProjectCommand
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import org.springframework.web.multipart.MultipartFile

data class UpdateProjectRequest(
    @field:Size(max = 100, message = "프로젝트 제목은 최대 100자까지 작성할 수 있습니다.")
    val title: String? = null,
    
    val description: String? = null,

    val githubUrl: String? = null,

    val deployUrl: String? = null,

    @field:NotEmpty(message = "기술 스택을 변경하려면 최소 1개 이상 입력해야 합니다.")
    val techStacks: List<String>? = null,

    val members: List<MemberAssignment>? = null,
) {
    data class MemberAssignment(val userId: UserId, val position: Position)

    fun toCommand(requesterId: UserId, projectId: ProjectId, thumbnailImage: MultipartFile?): UpdateProjectCommand =
        UpdateProjectCommand(
            projectId = projectId,
            requesterId = requesterId,
            title = title,
            description = description,
            githubUrl = githubUrl,
            deployUrl = deployUrl,
            techStacks = techStacks,
            members = members?.map { UpdateProjectCommand.MemberAssignment(it.userId, it.position) },
            thumbnailImage = thumbnailImage,
        )
}
