package kr.co.wground.gallery.application.usecase

import kr.co.wground.gallery.application.usecase.command.CreateProjectCommand
import kr.co.wground.gallery.application.usecase.command.DeleteProjectCommand
import kr.co.wground.gallery.application.usecase.command.UpdateProjectCommand
import kr.co.wground.global.common.ProjectId

interface ProjectCommandUseCase {
    fun create(command: CreateProjectCommand): ProjectId
    fun update(command: UpdateProjectCommand)
    fun delete(command: DeleteProjectCommand)
}
