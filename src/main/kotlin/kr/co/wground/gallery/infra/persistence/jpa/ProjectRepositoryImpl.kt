package kr.co.wground.gallery.infra.persistence.jpa

import kr.co.wground.gallery.application.repository.ProjectRepository
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.ProjectId
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository

@Repository
class ProjectRepositoryImpl(
    private val jpaRepository: ProjectJpaRepository,
) : ProjectRepository {
    override fun save(project: Project): Project = jpaRepository.save(project)
    override fun findById(id: ProjectId): Project? = jpaRepository.findByIdOrNull(id)
}
