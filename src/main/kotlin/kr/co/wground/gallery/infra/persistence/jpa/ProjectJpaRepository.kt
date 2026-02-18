package kr.co.wground.gallery.infra.persistence.jpa

import kr.co.wground.gallery.domain.model.Project
import org.springframework.data.jpa.repository.JpaRepository

interface ProjectJpaRepository : JpaRepository<Project, Long>
