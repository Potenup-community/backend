package kr.co.wground.gallery.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.global.common.UserId

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["project_id", "user_id"])
    ]
)
class ProjectMember private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @Column(name = "user_id", nullable = false)
    val userId: UserId,

    position: Position,
) {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var position: Position = position
        protected set

    fun updatePosition(position: Position) {
        this.position = position
    }

    companion object {
        fun create(project: Project, userId: UserId, position: Position): ProjectMember {
            return ProjectMember(
                project = project,
                userId = userId,
                position = position,
            )
        }
    }
}
