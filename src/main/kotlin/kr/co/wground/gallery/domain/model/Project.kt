package kr.co.wground.gallery.domain.model

import jakarta.persistence.CascadeType
import jakarta.persistence.Column
import jakarta.persistence.Embedded
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.OneToMany
import java.time.LocalDateTime
import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.global.common.ProjectId
import kr.co.wground.global.common.UserId

@Entity
class Project private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: ProjectId = 0,

    @Column(nullable = false)
    val authorId: UserId,

    content: ProjectContent,

    @Column(nullable = false, length = MAX_TECH_STACKS_LENGTH)
    var techStacks: String,

    @Column(nullable = false, length = MAX_URL_LENGTH)
    var githubUrl: String,

    @Column(length = MAX_URL_LENGTH)
    var deployUrl: String? = null,

    @Column(nullable = false, length = MAX_URL_LENGTH)
    var thumbnailImagePath: String,

    @Column(nullable = false)
    var viewCount: Int = 0,

    @Column(nullable = false)
    val createdAt: LocalDateTime = LocalDateTime.now(),

    modifiedAt: LocalDateTime = LocalDateTime.now(),

    deletedAt: LocalDateTime? = null,
) {
    @Embedded
    var content: ProjectContent = content
        protected set

    var modifiedAt: LocalDateTime = modifiedAt
        protected set

    var deletedAt: LocalDateTime? = deletedAt
        protected set

    @OneToMany(mappedBy = "project", cascade = [CascadeType.ALL], orphanRemoval = true, fetch = FetchType.EAGER)
    protected val _members: MutableList<ProjectMember> = ArrayList()
    val members: List<ProjectMember> get() = _members.toList()

    val isDeleted: Boolean get() = deletedAt != null

    companion object {
        const val MAX_TECH_STACKS_LENGTH = 500
        const val MAX_URL_LENGTH = 500
        val GITHUB_URL_PATTERN = Regex("^(http|https)://github.com.*$")
        val URL_PATTERN = Regex("^(http|https)://.*$")

        fun createNew(
            authorId: UserId,
            content: ProjectContent,
            techStacks: String,
            githubUrl: String,
            deployUrl: String? = null,
            thumbnailImagePath: String,
        ): Project {
            return Project(
                authorId = authorId,
                content = content,
                techStacks = techStacks,
                githubUrl = githubUrl,
                deployUrl = deployUrl,
                thumbnailImagePath = thumbnailImagePath,
            )
        }

    }

    init {
        validateTechStacks(techStacks)
        validateGithubUrl(githubUrl)
        deployUrl?.let { validateDeployUrl(it) }
        validateThumbnailImagePath(thumbnailImagePath)
    }

    fun update(
        content: ProjectContent,
        techStacks: String,
        githubUrl: String,
        deployUrl: String?,
        thumbnailImagePath: String,
    ) {
        validateAlive()
        validateTechStacks(techStacks)
        validateGithubUrl(githubUrl)
        if (deployUrl != null) {
            validateDeployUrl(deployUrl)
        }
        validateThumbnailImagePath(thumbnailImagePath)

        this.content = content
        this.techStacks = techStacks
        this.githubUrl = githubUrl
        this.deployUrl = deployUrl
        this.thumbnailImagePath = thumbnailImagePath
        this.modifiedAt = LocalDateTime.now()
    }

    fun softDelete() {
        validateAlive()
        this.deletedAt = LocalDateTime.now()
    }

    fun incrementViewCount() {
        this.viewCount++
    }

    fun addMember(userId: UserId, position: String) {
        if (_members.any { it.userId == userId }) {
            return
        }
        _members.add(ProjectMember.create(project = this, userId = userId, position = position))
    }

    fun updateMembers(members: List<MemberInfo>) {
        val newUserIds = members.map { it.userId }.toSet()

        val iterator = _members.iterator()
        while (iterator.hasNext()) {
            val member = iterator.next()
            if (member.userId !in newUserIds) {
                iterator.remove()
            }
        }

        members.forEach { info ->
            if (_members.none { it.userId == info.userId }) {
                _members.add(ProjectMember.create(project = this, userId = info.userId, position = info.position))
            }
        }
    }

    data class MemberInfo(val userId: UserId, val position: String)

    private fun validateAlive() {
        if (isDeleted) {
            throw BusinessException(ProjectErrorCode.PROJECT_ALREADY_DELETED)
        }
    }

    private fun validateTechStacks(techStacks: String) {
        if (techStacks.isBlank()) {
            throw BusinessException(ProjectErrorCode.TECH_STACK_REQUIRED)
        }
    }

    private fun validateGithubUrl(githubUrl: String) {
        if (!GITHUB_URL_PATTERN.matches(githubUrl)) {
            throw BusinessException(ProjectErrorCode.INVALID_GITHUB_URL)
        }
    }

    private fun validateDeployUrl(deployUrl: String) {
        if (!URL_PATTERN.matches(deployUrl)) {
            throw BusinessException(ProjectErrorCode.INVALID_DEPLOY_URL)
        }
    }

    private fun validateThumbnailImagePath(thumbnailImagePath: String) {
        if (thumbnailImagePath.isBlank()) {
            throw BusinessException(ProjectErrorCode.THUMBNAIL_REQUIRED)
        }
    }
}
