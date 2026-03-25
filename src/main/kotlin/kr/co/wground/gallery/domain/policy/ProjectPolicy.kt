package kr.co.wground.gallery.domain.policy

import kr.co.wground.exception.BusinessException
import kr.co.wground.gallery.domain.exception.ProjectErrorCode
import kr.co.wground.gallery.domain.model.Project
import kr.co.wground.global.common.UserId
import kr.co.wground.user.domain.constant.UserRole

object ProjectPolicy {

    fun validateModifiable(project: Project, userId: UserId, userRole: UserRole) {
        if (project.authorId != userId && userRole != UserRole.ADMIN) {
            throw BusinessException(ProjectErrorCode.PROJECT_ACCESS_DENIED)
        }
    }

    fun validateDeletable(project: Project, userId: UserId, userRole: UserRole) {
        if (project.authorId != userId && userRole != UserRole.ADMIN) {
            throw BusinessException(ProjectErrorCode.PROJECT_ACCESS_DENIED)
        }
    }
}
