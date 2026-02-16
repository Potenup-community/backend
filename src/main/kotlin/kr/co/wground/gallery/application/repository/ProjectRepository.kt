package kr.co.wground.gallery.application.repository

import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.ProjectId

interface ProjectRepository {
    fun save(project: Project): Project
    fun findById(id: ProjectId): Project?
}
