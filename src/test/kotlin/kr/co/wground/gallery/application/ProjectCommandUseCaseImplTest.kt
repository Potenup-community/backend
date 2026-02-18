package kr.co.wground.gallery.application

import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import io.mockk.slot
import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.application.usecase.command.CreateProjectCommand
import kr.co.wground.gallery.application.usecase.impl.ProjectCommandUseCaseImpl
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.TrackId
import kr.co.wground.image.application.ImageStorageService
import kr.co.wground.user.domain.User
import kr.co.wground.user.domain.constant.UserRole
import kr.co.wground.user.domain.constant.UserStatus
import kr.co.wground.user.infra.UserRepository
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.springframework.mock.web.MockMultipartFile

@ExtendWith(MockKExtension::class)
class ProjectCommandUseCaseImplTest {

    @MockK
    private lateinit var projectRepository: ProjectRepository

    @MockK
    private lateinit var userRepository: UserRepository

    @MockK
    private lateinit var imageStorageService: ImageStorageService

    private lateinit var useCase: ProjectCommandUseCaseImpl

    @BeforeEach
    fun setUp() {
        useCase = ProjectCommandUseCaseImpl(projectRepository, userRepository, imageStorageService)
    }

    @Nested
    @DisplayName("프로젝트 생성")
    inner class Create {

        @Test
        @DisplayName("유효한 커맨드로 프로젝트를 생성할 수 있다")
        fun shouldCreateProject_whenValidCommand() {
            // given
            val command = createValidCommand()
            stubDependencies(
                users = listOf(
                    createUser(10L, "김철수", 2L),
                    createUser(20L, "홍길동", 1L),
                ),
            )

            // when
            val result = useCase.create(command)

            // then
            assertThat(result).isNotNull()
        }

        @Test
        @DisplayName("등록자가 멤버 목록에 없으면 자동으로 추가된다")
        fun shouldAutoIncludeAuthor_whenNotInMembers() {
            // given
            val command = createValidCommand(
                authorId = 1L,
                members = listOf(
                    CreateProjectCommand.MemberAssignment("홍길동", 1L, Position.FRONTEND),
                ),
            )
            val projectSlot = slot<Project>()
            stubDependencies(
                users = listOf(createUser(10L, "홍길동", 1L)),
                projectSlot = projectSlot,
            )

            // when
            useCase.create(command)

            // then
            val saved = projectSlot.captured
            assertThat(saved.members).hasSize(2)
            assertThat(saved.members.map { it.userId }).containsExactlyInAnyOrder(1L, 10L)
        }

        @Test
        @DisplayName("등록자가 이미 멤버 목록에 있으면 중복 추가하지 않는다")
        fun shouldNotDuplicateAuthor_whenAlreadyInMembers() {
            // given
            val command = createValidCommand(
                authorId = 1L,
                members = listOf(
                    CreateProjectCommand.MemberAssignment("홍길동", 1L, Position.BACKEND),
                    CreateProjectCommand.MemberAssignment("김철수", 2L, Position.FRONTEND),
                ),
            )
            val projectSlot = slot<Project>()
            stubDependencies(
                users = listOf(
                    createUser(1L, "홍길동", 1L),  // authorId와 동일한 userId → 중복 추가 안 됨
                    createUser(10L, "김철수", 2L),
                ),
                projectSlot = projectSlot,
            )

            // when
            useCase.create(command)

            // then
            assertThat(projectSlot.captured.members).hasSize(2)
        }

        @Test
        @DisplayName("존재하지 않는 유저가 멤버에 포함되면 예외가 발생한다")
        fun shouldThrowException_whenMemberNotFound() {
            // given
            val command = createValidCommand(
                members = listOf(
                    CreateProjectCommand.MemberAssignment("김철수", 10L, Position.BACKEND),
                    CreateProjectCommand.MemberAssignment("존재하지않는유저", 999L, Position.FRONTEND),
                ),
            )
            every { userRepository.findByNameInAndTrackIdIn(any(), any()) } returns listOf(
                createUser(10L, "김철수", 10L),
            )

            // when & then
            assertThatThrownBy { useCase.create(command) }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.MEMBER_NOT_FOUND.code)
        }
    }

    private fun stubDependencies(
        users: List<User>,
        projectSlot: io.mockk.CapturingSlot<Project>? = null,
    ) {
        every { userRepository.findByNameInAndTrackIdIn(any(), any()) } returns users
        every { imageStorageService.saveProjectThumbnail(any(), any()) } returns "projects/1/thumb.png"
        if (projectSlot != null) {
            every { projectRepository.save(capture(projectSlot)) } answers { projectSlot.captured }
        } else {
            every { projectRepository.save(any()) } answers { firstArg() }
        }
    }

    private fun createValidCommand(
        authorId: Long = 1L,
        members: List<CreateProjectCommand.MemberAssignment> = listOf(
            CreateProjectCommand.MemberAssignment("김철수", 2L, Position.BACKEND),
            CreateProjectCommand.MemberAssignment("홍길동", 1L, Position.FRONTEND),
        ),
    ): CreateProjectCommand = CreateProjectCommand(
        authorId = authorId,
        title = "테스트 프로젝트",
        description = "테스트 설명입니다",
        githubUrl = "https://github.com/test/project",
        deployUrl = "https://deploy.example.com",
        techStacks = listOf("Kotlin", "Spring Boot"),
        members = members,
        thumbnailImage = MockMultipartFile("thumbnail", "thumb.png", "image/png", "fake-image".toByteArray()),
    )

    private fun createUser(userId: Long, name: String, trackId: TrackId): User = User(
        userId = userId,
        trackId = trackId,
        email = "user$userId@test.com",
        name = name,
        phoneNumber = "010-0000-${userId.toString().padStart(4, '0')}",
        provider = "google",
        role = UserRole.MEMBER,
        status = UserStatus.ACTIVE,
    )
}
