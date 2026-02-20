package kr.co.wground.gallery.presentation.request

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotEmpty
import jakarta.validation.constraints.Size
import kr.co.wground.gallery.application.usecase.command.CreateProjectCommand
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.global.common.TrackId
import kr.co.wground.global.common.UserId
import org.springframework.web.multipart.MultipartFile

data class CreateProjectRequest(
    @field:NotBlank(message = "프로젝트 제목은 필수입니다.")
    @field:Size(max = 100, message = "프로젝트 제목은 최대 100자까지 작성할 수 있습니다.")
    val title: String,

    @field:NotBlank(message = "프로젝트 설명은 필수입니다.")
    val description: String,

    @field:NotBlank(message = "GitHub URL은 필수입니다.")
    val githubUrl: String,

    val deployUrl: String? = null,

    @field:NotEmpty(message = "기술 스택은 최소 1개 이상 입력해야 합니다.")
    val techStacks: List<String>,

    val members: List<MemberAssignment> = emptyList(),
) {
    data class MemberAssignment(val name: String, val trackId: TrackId, val position: Position)

    fun toCommand(authorId: UserId, thumbnailImage: MultipartFile): CreateProjectCommand {
        return CreateProjectCommand(
            authorId = authorId,
            title = title,
            description = description,
            githubUrl = githubUrl,
            deployUrl = deployUrl,
            techStacks = techStacks,
            members = members.map { CreateProjectCommand.MemberAssignment(it.name, it.trackId, it.position) },
            thumbnailImage = thumbnailImage,
        )
    }
}
