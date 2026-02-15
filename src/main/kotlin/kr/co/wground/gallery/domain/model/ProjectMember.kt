package kr.co.wground.gallery.domain.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.FetchType
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.JoinColumn
import jakarta.persistence.ManyToOne
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.global.common.UserId
import lombok.AccessLevel
import lombok.NoArgsConstructor

@Entity
@Table(
    uniqueConstraints = [
        UniqueConstraint(columnNames = ["project_id", "user_id"])
    ]
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
class ProjectMember private constructor(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "project_id", nullable = false)
    val project: Project,

    @Column(name = "user_id", nullable = false)
    val userId: UserId,

    @Column(nullable = false, length = MAX_POSITION_LENGTH)
    val position: String,
) {

    companion object {
        const val MAX_POSITION_LENGTH = 50

        fun create(project: Project, userId: UserId, position: String): ProjectMember {
            return ProjectMember(
                project = project,
                userId = userId,
                position = position,
            )
        }
    }
}
