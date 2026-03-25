package kr.co.wground.gallery.domain

import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.gallery.domain.model.Position
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.gallery.domain.model.ProjectContent
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource

class ProjectTest {

    @Nested
    @DisplayName("프로젝트 생성")
    inner class Create {

        @Test
        @DisplayName("유효한 값으로 프로젝트를 생성할 수 있다")
        fun shouldCreateProject_whenValidInput() {
            val project = createValidProject()

            assertThat(project.authorId).isEqualTo(1L)
            assertThat(project.content.title).isEqualTo("테스트 프로젝트")
            assertThat(project.techStacks).isEqualTo("Kotlin,Spring Boot")
            assertThat(project.githubUrl).isEqualTo("https://github.com/test/project")
            assertThat(project.viewCount).isEqualTo(0)
            assertThat(project.isDeleted).isFalse()
        }

        @Test
        @DisplayName("deployUrl이 null이어도 프로젝트를 생성할 수 있다")
        fun shouldCreateProject_whenDeployUrlIsNull() {
            val project = createValidProject(deployUrl = null)

            assertThat(project.deployUrl).isNull()
        }

        @Test
        @DisplayName("techStacks가 빈 문자열이면 예외가 발생한다")
        fun shouldThrowException_whenTechStacksIsBlank() {
            assertThatThrownBy { createValidProject(techStacks = "") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.TECH_STACK_REQUIRED.code)
        }

        @ParameterizedTest(name = "githubUrl: {0}")
        @ValueSource(strings = ["", "not-a-url", "http://gitlab.com/repo", "ftp://github.com/repo"])
        @DisplayName("유효하지 않은 GitHub URL이면 예외가 발생한다")
        fun shouldThrowException_whenGithubUrlIsInvalid(invalidUrl: String) {
            assertThatThrownBy { createValidProject(githubUrl = invalidUrl) }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_GITHUB_URL.code)
        }

        @ParameterizedTest(name = "githubUrl: {0}")
        @ValueSource(strings = ["https://github.com/test/repo", "http://github.com/test/repo"])
        @DisplayName("http, https 모두 유효한 GitHub URL로 인정한다")
        fun shouldAcceptGithubUrl_whenProtocolIsHttpOrHttps(validUrl: String) {
            val project = createValidProject(githubUrl = validUrl)

            assertThat(project.githubUrl).isEqualTo(validUrl)
        }

        @Test
        @DisplayName("유효하지 않은 배포 URL이면 예외가 발생한다")
        fun shouldThrowException_whenDeployUrlIsInvalid() {
            assertThatThrownBy { createValidProject(deployUrl = "not-a-url") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_DEPLOY_URL.code)
        }

        @Test
        @DisplayName("thumbnailImagePath가 빈 문자열이면 예외가 발생한다")
        fun shouldThrowException_whenThumbnailImagePathIsBlank() {
            assertThatThrownBy { createValidProject(thumbnailImagePath = "") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.THUMBNAIL_REQUIRED.code)
        }
    }

    @Nested
    @DisplayName("프로젝트 수정")
    inner class Update {

        @Test
        @DisplayName("유효한 값으로 프로젝트를 수정할 수 있다")
        fun shouldUpdateProject_whenValidInput() {
            val project = createValidProject()
            val newContent = ProjectContent(title = "수정된 제목", description = "수정된 설명")

            project.update(
                content = newContent,
                techStacks = "React,TypeScript",
                githubUrl = "https://github.com/updated/repo",
                deployUrl = "https://updated.com",
                thumbnailImagePath = "/images/updated.png",
            )

            assertThat(project.content.title).isEqualTo("수정된 제목")
            assertThat(project.techStacks).isEqualTo("React,TypeScript")
            assertThat(project.githubUrl).isEqualTo("https://github.com/updated/repo")
            assertThat(project.deployUrl).isEqualTo("https://updated.com")
        }

        @Test
        @DisplayName("삭제된 프로젝트는 수정할 수 없다")
        fun shouldThrowException_whenUpdateDeletedProject() {
            val project = createValidProject()
            project.softDelete()

            assertThatThrownBy {
                project.update(
                    content = project.content,
                    techStacks = project.techStacks,
                    githubUrl = project.githubUrl,
                    deployUrl = project.deployUrl,
                    thumbnailImagePath = project.thumbnailImagePath,
                )
            }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.PROJECT_ALREADY_DELETED.code)
        }

        @Test
        @DisplayName("수정 시 deployUrl을 null로 변경할 수 있다")
        fun shouldAllowNullDeployUrl_whenUpdate() {
            val project = createValidProject(deployUrl = "https://deploy.com")

            project.update(
                content = project.content,
                techStacks = project.techStacks,
                githubUrl = project.githubUrl,
                deployUrl = null,
                thumbnailImagePath = project.thumbnailImagePath,
            )

            assertThat(project.deployUrl).isNull()
        }
    }

    @Nested
    @DisplayName("프로젝트 삭제")
    inner class SoftDelete {

        @Test
        @DisplayName("프로젝트를 소프트 삭제할 수 있다")
        fun shouldSoftDelete_whenAlive() {
            val project = createValidProject()

            project.softDelete()

            assertThat(project.isDeleted).isTrue()
            assertThat(project.deletedAt).isNotNull()
        }

        @Test
        @DisplayName("이미 삭제된 프로젝트를 다시 삭제하면 예외가 발생한다")
        fun shouldThrowException_whenAlreadyDeleted() {
            val project = createValidProject()
            project.softDelete()

            assertThatThrownBy { project.softDelete() }
                .isInstanceOf(BusinessException::class.java)
                .hasMessageContaining("이미 삭제된 프로젝트")
        }
    }

    @Nested
    @DisplayName("멤버 추가")
    inner class AddMember {

        @Test
        @DisplayName("멤버를 추가할 수 있다")
        fun shouldAddMember() {
            val project = createValidProject()

            project.addMember(10L, Position.BACKEND)

            assertThat(project.members).hasSize(1)
            assertThat(project.members[0].userId).isEqualTo(10L)
            assertThat(project.members[0].position).isEqualTo(Position.BACKEND)
        }

        @Test
        @DisplayName("동일한 유저를 중복 추가하면 무시된다")
        fun shouldIgnoreDuplicate_whenSameUserAdded() {
            val project = createValidProject()

            project.addMember(10L, Position.BACKEND)
            project.addMember(10L, Position.FRONTEND)

            assertThat(project.members).hasSize(1)
            assertThat(project.members[0].position).isEqualTo(Position.BACKEND)
        }
    }

    @Nested
    @DisplayName("멤버 목록 수정")
    inner class UpdateMembers {

        @Test
        @DisplayName("새로운 멤버가 추가되고 기존 멤버가 제거된다")
        fun shouldAddNewAndRemoveOld() {
            val project = createValidProject()
            project.addMember(10L, Position.BACKEND)
            project.addMember(20L, Position.FRONTEND)

            project.updateMembers(
                listOf(
                    Project.MemberInfo(20L, Position.FRONTEND),
                    Project.MemberInfo(30L, Position.DESIGN),
                )
            )

            val memberUserIds = project.members.map { it.userId }
            assertThat(memberUserIds).containsExactlyInAnyOrder(20L, 30L)
        }

        @Test
        @DisplayName("기존 멤버의 포지션이 변경된다")
        fun shouldUpdatePosition_whenMemberExists() {
            val project = createValidProject()
            project.addMember(10L, Position.BACKEND)

            project.updateMembers(
                listOf(Project.MemberInfo(10L, Position.PM))
            )

            assertThat(project.members).hasSize(1)
            assertThat(project.members[0].position).isEqualTo(Position.PM)
        }

        @Test
        @DisplayName("빈 목록으로 수정하면 모든 멤버가 제거된다")
        fun shouldRemoveAllMembers_whenEmptyList() {
            val project = createValidProject()
            project.addMember(10L, Position.BACKEND)
            project.addMember(20L, Position.FRONTEND)

            project.updateMembers(emptyList())

            assertThat(project.members).isEmpty()
        }
    }

    @Nested
    @DisplayName("ProjectContent 생성")
    inner class ContentValidation {

        @Test
        @DisplayName("유효한 값으로 ProjectContent를 생성할 수 있다")
        fun shouldCreateContent_whenValid() {
            val content = ProjectContent(title = "제목", description = "설명")

            assertThat(content.title).isEqualTo("제목")
            assertThat(content.description).isEqualTo("설명")
        }

        @Test
        @DisplayName("제목이 빈 문자열이면 예외가 발생한다")
        fun shouldThrowException_whenTitleIsBlank() {
            assertThatThrownBy { ProjectContent(title = "", description = "설명") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_PROJECT_TITLE.code)
        }

        @Test
        @DisplayName("제목이 100자를 초과하면 예외가 발생한다")
        fun shouldThrowException_whenTitleExceedsMaxLength() {
            val longTitle = "가".repeat(101)

            assertThatThrownBy { ProjectContent(title = longTitle, description = "설명") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_PROJECT_TITLE.code)
        }

        @Test
        @DisplayName("설명이 빈 문자열이면 예외가 발생한다")
        fun shouldThrowException_whenDescriptionIsBlank() {
            assertThatThrownBy { ProjectContent(title = "제목", description = "") }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_PROJECT_DESCRIPTION.code)
        }

        @Test
        @DisplayName("설명이 2000자를 초과하면 예외가 발생한다")
        fun shouldThrowException_whenDescriptionExceedsMaxLength() {
            val longDescription = "가".repeat(2001)

            assertThatThrownBy { ProjectContent(title = "제목", description = longDescription) }
                .isInstanceOf(BusinessException::class.java)
                .extracting("code")
                .isEqualTo(ProjectErrorCode.INVALID_PROJECT_DESCRIPTION.code)
        }

        @Test
        @DisplayName("content를 부분 수정할 수 있다")
        fun shouldUpdatePartially() {
            val content = ProjectContent(title = "원래 제목", description = "원래 설명")

            val updated = content.update(title = "수정된 제목", description = null)

            assertThat(updated.title).isEqualTo("수정된 제목")
            assertThat(updated.description).isEqualTo("원래 설명")
        }
    }

    private fun createValidProject(
        authorId: Long = 1L,
        techStacks: String = "Kotlin,Spring Boot",
        githubUrl: String = "https://github.com/test/project",
        deployUrl: String? = "https://deploy.example.com",
        thumbnailImagePath: String = "/images/thumbnail.png",
    ): Project = Project.create(
        authorId = authorId,
        content = ProjectContent(title = "테스트 프로젝트", description = "테스트 설명"),
        techStacks = techStacks,
        githubUrl = githubUrl,
        deployUrl = deployUrl,
        thumbnailImagePath = thumbnailImagePath,
    )
}
