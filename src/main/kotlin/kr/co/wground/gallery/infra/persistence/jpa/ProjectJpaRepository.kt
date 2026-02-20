package kr.co.wground.gallery.infra.persistence.jpa

import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.ProjectId
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Modifying
import org.springframework.data.jpa.repository.Query

interface ProjectJpaRepository : JpaRepository<Project, Long> {

    @Modifying
    @Query("UPDATE Project p SET p.viewCount = p.viewCount + 1 WHERE p.id = :projectId")
    fun incrementViewCount(projectId: ProjectId)
}
