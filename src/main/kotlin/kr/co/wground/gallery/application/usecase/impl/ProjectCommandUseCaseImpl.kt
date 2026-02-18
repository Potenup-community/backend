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
import kr.co.wground.global.common.UserId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.user.infra.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

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

        allMembers.forEach { (userId, position) ->
            project.addMember(userId, position)
        }

        return projectRepository.save(project).id
    }

    /**
     * (name, trackId) 쌍으로 User를 조회해 (userId, position) 쌍으로 변환한다.
     *
     * 동명이인은 trackId로 구분할 수 있다 — 같은 트랙 안에서 동명이인이 있는 극단적 경우는
     * 현재 요구사항 범위 밖이므로 그 경우 첫 번째로 매칭된 사용자를 반환한다.
     * 어떤 (name, trackId) 쌍과도 매칭되는 사용자가 없으면 MEMBER_NOT_FOUND 예외를 던진다.
     */
    private fun resolveMembers(
        assignments: List<CreateProjectCommand.MemberAssignment>,
    ): List<Pair<UserId, Position>> {
        if (assignments.isEmpty()) return emptyList()

        val names = assignments.map { it.name }
        val trackIds = assignments.map { it.trackId }
        val foundUsers = userRepository.findByNameInAndTrackIdIn(names, trackIds)

        val userLookup = foundUsers.associateBy { it.name to it.trackId }

        return assignments.map { assignment ->
            val user = userLookup[assignment.name to assignment.trackId]
                ?: throw BusinessException(ProjectErrorCode.MEMBER_NOT_FOUND)
            user.userId to assignment.position
        }
    }

    /**
     * 등록한 사용자가 멤버 목록에 없으면 OTHER 포지션으로 자동 포함한다.
     * 등록한 사용자는 반드시 프로젝트 멤버여야 하므로 누락 시 조용히 추가한다.
     */
    private fun ensureAuthorIncluded(
        authorId: UserId,
        members: List<Pair<UserId, Position>>,
    ): List<Pair<UserId, Position>> =
        if (members.any { it.first == authorId }) members
        else members + (authorId to Position.OTHER)
}
