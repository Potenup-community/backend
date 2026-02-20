package kr.co.wground.gallery.application.usecase.impl

import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.application.usecase.ProjectCommandUseCase
import kr.co.wground.gallery.application.usecase.command.CreateProjectCommand
import kr.co.wground.gallery.application.usecase.command.DeleteProjectCommand
import kr.co.wground.gallery.application.usecase.command.UpdateProjectCommand
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.gallery.domain.model.ProjectContent
import kr.co.wground.gallery.domain.policy.ProjectPolicy
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.user.infra.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import org.springframework.transaction.support.TransactionSynchronization
import org.springframework.transaction.support.TransactionSynchronizationManager

@Service
@Transactional
class ProjectCommandUseCaseImpl(
    private val projectRepository: ProjectRepository,
    private val userRepository: UserRepository,
    private val imageStorageService: ImageStorageService,
) : ProjectCommandUseCase {

    override fun create(command: CreateProjectCommand): ProjectId {
        val resolvedMembers = resolveMembers(command.members)
        val allMembers = ensureAuthorIncluded(command.authorId, resolvedMembers)

        val thumbnailPath = imageStorageService.saveProjectThumbnail(command.authorId, command.thumbnailImage)

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

        allMembers.forEach { project.addMember(it.userId, it.position) }

        return projectRepository.save(project).id
    }

    override fun update(command: UpdateProjectCommand) {
        val project = projectRepository.findById(command.projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val requester = requireNotNull(userRepository.findByUserIdIn(listOf(command.requesterId)).firstOrNull())
        ProjectPolicy.validateModifiable(project, command.requesterId, requester.role)

        command.members?.let { assignments ->
            val resolvedMembers = resolveUpdateMembers(assignments)
            project.updateMembers(ensureAuthorIncluded(project.authorId, resolvedMembers))
        }

        val newThumbnailPath = command.thumbnailImage?.let { image ->
            val oldPath = project.thumbnailImagePath
            val newPath = imageStorageService.saveProjectThumbnail(command.requesterId, image)
            TransactionSynchronizationManager.registerSynchronization(object : TransactionSynchronization {
                override fun afterCommit() {
                    imageStorageService.deleteByRelativePath(oldPath)
                }
            })
            newPath
        } ?: project.thumbnailImagePath
        
        val resolvedDeployUrl = when (val d = command.deployUrl) {
            null -> project.deployUrl
            "" -> null
            else -> d
        }

        project.update(
            content = ProjectContent(
                title = command.title ?: project.content.title,
                description = command.description ?: project.content.description,
            ),
            techStacks = command.techStacks?.joinToString(",") ?: project.techStacks,
            githubUrl = command.githubUrl ?: project.githubUrl,
            deployUrl = resolvedDeployUrl,
            thumbnailImagePath = newThumbnailPath,
        )
    }

    override fun delete(command: DeleteProjectCommand) {
        val project = projectRepository.findById(command.projectId)
            ?: throw BusinessException(ProjectErrorCode.PROJECT_NOT_FOUND)

        val requester = requireNotNull(userRepository.findByUserIdIn(listOf(command.requesterId)).firstOrNull())
        ProjectPolicy.validateDeletable(project, command.requesterId, requester.role)

        project.softDelete()
    }

    private fun resolveMembers(
        assignments: List<CreateProjectCommand.MemberAssignment>,
    ): List<Project.MemberInfo> =
        validateAndMapMembers(assignments.map { Project.MemberInfo(it.userId, it.position) })

    private fun resolveUpdateMembers(
        assignments: List<UpdateProjectCommand.MemberAssignment>,
    ): List<Project.MemberInfo> =
        validateAndMapMembers(assignments.map { Project.MemberInfo(it.userId, it.position) })

    private fun validateAndMapMembers(members: List<Project.MemberInfo>): List<Project.MemberInfo> {
        if (members.isEmpty()) return emptyList()

        val requestedIds = members.mapTo(HashSet()) { it.userId }
        val foundIds = userRepository.findByUserIdIn(requestedIds.toList()).mapTo(HashSet()) { it.userId }

        if (!foundIds.containsAll(requestedIds)) throw BusinessException(ProjectErrorCode.MEMBER_NOT_FOUND)

        return members
    }

    /**
     * 원저자가 멤버 목록에 없으면 OTHER 포지션으로 자동 포함한다.
     * 등록/수정 시 원저자는 반드시 프로젝트 멤버여야 하므로 누락 시 조용히 추가한다.
     */
    private fun ensureAuthorIncluded(
        authorId: UserId,
        members: List<Project.MemberInfo>,
    ): List<Project.MemberInfo> =
        if (members.any { it.userId == authorId }) members
        else members + Project.MemberInfo(authorId, Position.OTHER)
}
