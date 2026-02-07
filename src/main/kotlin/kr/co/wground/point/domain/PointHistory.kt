package kr.co.wground.point.domain

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Index
import jakarta.persistence.PrePersist
import jakarta.persistence.Table
import jakarta.persistence.UniqueConstraint
import kr.co.wground.exception.BusinessException
import kr.co.wground.global.common.UserId
import kr.co.wground.point.exception.PointErrorCode
import java.time.LocalDateTime

@Entity
@Table(
    name = "point_history",
    uniqueConstraints = [
        UniqueConstraint(
            name = "uk_point_history_idempotency",
            columnNames = ["user_id", "ref_type", "ref_id", "type"]
        )
    ],
    indexes = [
        Index(name = "idx_point_history_created_at", columnList = "created_at"),
        Index(name = "idx_point_history_user_type_created", columnList = "user_id, type, created_at"),
        Index(name = "idx_point_history_ref", columnList = "ref_type, ref_id")
    ]
)
class PointHistory private constructor(
    @Column(name = "user_id", nullable = false, updatable = false)
    val userId: UserId,

    @Column(nullable = false, updatable = false)
    val amount: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    val type: PointType,

    @Enumerated(EnumType.STRING)
    @Column(name = "ref_type", nullable = false, updatable = false, length = 20)
    val refType: ReferenceType,

    @Column(name = "ref_id", nullable = false, updatable = false)
    val refId: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null
        protected set

    @Column(name = "created_at", nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now()
        protected set

    @PrePersist
    fun onCreate() {
        createdAt = LocalDateTime.now()
    }

    companion object {
        fun create(
            userId: UserId,
            amount: Long,
            type: PointType,
            refType: ReferenceType,
            refId: Long
        ): PointHistory {
            validate(userId, amount, refId)
            return PointHistory(userId, amount, type, refType, refId)
        }

        //좋아요 받았을 때
        fun forPostLikeReward(userId: UserId, reactionId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.RECEIVE_LIKE_POST.amount,
                type = PointType.RECEIVE_LIKE_POST,
                refType = ReferenceType.POST_REACTION,
                refId = reactionId
            )
        }

        fun forCommentLikeReward(userId: UserId, reactionId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.RECEIVE_LIKE_COMMENT.amount,
                type = PointType.RECEIVE_LIKE_COMMENT,
                refType = ReferenceType.COMMENT_REACTION,
                refId = reactionId
            )
        }

        //좋아요 눌렀을때
        fun forGivePostLike(userId: UserId, reactionId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.GIVE_LIKE_POST.amount,
                type = PointType.GIVE_LIKE_POST,
                refType = ReferenceType.POST_REACTION,
                refId = reactionId
            )
        }

        fun forGiveCommentLike(userId: UserId, reactionId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.GIVE_LIKE_COMMENT.amount,
                type = PointType.GIVE_LIKE_COMMENT,
                refType = ReferenceType.COMMENT_REACTION,
                refId = reactionId
            )
        }

        fun forWritePost(userId: UserId, postId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.WRITE_POST.amount,
                type = PointType.WRITE_POST,
                refType = ReferenceType.POST,
                refId = postId
            )
        }

        fun forWriteComment(userId: UserId, commentId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.WRITE_COMMENT.amount,
                type = PointType.WRITE_COMMENT,
                refType = ReferenceType.COMMENT,
                refId = commentId
            )
        }

        fun forAttendance(userId: UserId, attendanceId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.ATTENDANCE.amount,
                type = PointType.ATTENDANCE,
                refType = ReferenceType.ATTENDANCE,
                refId = attendanceId
            )
        }

        fun forAttendanceStreak(userId: UserId, attendanceId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.ATTENDANCE_STREAK.amount,
                type = PointType.ATTENDANCE_STREAK,
                refType = ReferenceType.ATTENDANCE,
                refId = attendanceId
            )
        }

        fun forStudyCreate(userId: UserId, studyId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.STUDY_CREATE.amount,
                type = PointType.STUDY_CREATE,
                refType = ReferenceType.STUDY,
                refId = studyId
            )
        }

        fun forStudyJoin(userId: UserId, studyId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.STUDY_JOIN.amount,
                type = PointType.STUDY_JOIN,
                refType = ReferenceType.STUDY,
                refId = studyId
            )
        }

        fun forPurchase(userId: UserId, amount: Long, itemId: Long): PointHistory {
            return create(
                userId = userId,
                amount = -amount,
                type = PointType.USE_SHOP,
                refType = ReferenceType.SHOP_ITEM,
                refId = itemId
            )
        }

        private fun validate(userId: UserId, amount: Long, refId: Long) {
            if (userId <= 0) {
                throw BusinessException(PointErrorCode.INVALID_USER_ID)
            }
            if (amount == 0L) {
                throw BusinessException(PointErrorCode.INVALID_AMOUNT)
            }
            if (refId <= 0) {
                throw BusinessException(PointErrorCode.INVALID_REF_ID)
            }
        }
    }
}