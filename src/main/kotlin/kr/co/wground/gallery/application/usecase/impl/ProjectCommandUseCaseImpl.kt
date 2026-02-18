package kr.co.wground.gallery.application.usecase.impl

import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.application.usecase.ProjectCommandUseCase
import kr.co.wground.gallery.application.usecase.command.CreateProjectCommand
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.gallery.domain.model.ProjectContent
import kr.co.wground.global.common.ProjectId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.user.infra.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kr.co.wground.global.common.UserId

@Service
@Transactional
class ProjectCommandUseCaseImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val imageStorageService: ImageStorageService,
) : ProjectCommandUseCase {

    override fun create(command: CreateProjectCommand): ProjectId {
        val allMembers = ensureAuthorIncluded(command)
        val allMemberIds = allMembers.map { it.userId }

        validateMembersExist(allMemberIds)

        val thumbnailPath = saveThumbnail(command)

        val project = Project.create(
            authorId = command.authorId,
            content = ProjectContent(
                title = command.title,
                description = command.description,
            ),
            techStacks = command.techStacks.joinToString(","),
            githubUrl = command.githubUrl,
            deployUrl = command.deployUrl,
            thumbnailImagePath = thumbnailPath,
        )

        allMembers.forEach { member ->
            project.addMember(member.userId, member.position)
        }

        val saved = projectRepository.save(project)
        return saved.id
    }

    private fun ensureAuthorIncluded(command: CreateProjectCommand): List<CreateProjectCommand.MemberAssignment> {
        val hasAuthor = command.members.any { it.userId == command.authorId }
        if (hasAuthor) return command.members
        return command.members + CreateProjectCommand.MemberAssignment(command.authorId, Position.OTHER)
    }

    private fun validateMembersExist(memberIds: List<UserId>) {
        val foundUsers = userRepository.findByUserIdIn(memberIds)
        if (foundUsers.size != memberIds.distinct().size) {
            throw BusinessException(ProjectErrorCode.MEMBER_NOT_FOUND)
        }
    }

    private fun saveThumbnail(command: CreateProjectCommand): String =
        imageStorageService.saveProjectThumbnail(command.authorId, command.thumbnailImage)
}
