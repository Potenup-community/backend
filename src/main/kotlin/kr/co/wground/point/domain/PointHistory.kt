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
import java.time.format.DateTimeFormatter

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
    @Column(nullable = false, updatable = false)
    val userId: UserId,

    @Column(nullable = false, updatable = false)
    val amount: Long,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 30)
    val type: PointType,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, updatable = false, length = 20)
    val refType: PointReferenceType,

    @Column(nullable = false, updatable = false)
    val refId: Long
) {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0

    @Column(nullable = false, updatable = false)
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
            refType: PointReferenceType,
            refId: Long
        ): PointHistory {
            validate(userId, amount, refId)
            return PointHistory(userId, amount, type, refType, refId)
        }

        // 좋아요 받았을 때 (작성자 보상)
        fun forPostLikeReward(authorId: UserId, reactorId: UserId): PointHistory {
            return create(
                userId = authorId,
                amount = PointType.RECEIVE_LIKE_POST.amount,
                type = PointType.RECEIVE_LIKE_POST,
                refType = PointReferenceType.POST_REACTION,
                refId = reactorId
            )
        }

        fun forCommentLikeReward(authorId: UserId, reactorId: UserId): PointHistory {
            return create(
                userId = authorId,
                amount = PointType.RECEIVE_LIKE_COMMENT.amount,
                type = PointType.RECEIVE_LIKE_COMMENT,
                refType = PointReferenceType.COMMENT_REACTION,
                refId = reactorId
            )
        }

        // 좋아요 눌렀을 때 (누른 사람 보상)
        fun forGivePostLike(reactorId: UserId, postId: Long): PointHistory {
            return create(
                userId = reactorId,
                amount = PointType.GIVE_LIKE_POST.amount,
                type = PointType.GIVE_LIKE_POST,
                refType = PointReferenceType.POST_REACTION,
                refId = postId
            )
        }

        fun forGiveCommentLike(reactorId: UserId, commentId: Long): PointHistory {
            return create(
                userId = reactorId,
                amount = PointType.GIVE_LIKE_COMMENT.amount,
                type = PointType.GIVE_LIKE_COMMENT,
                refType = PointReferenceType.COMMENT_REACTION,
                refId = commentId
            )
        }

        fun forWritePost(userId: UserId, postId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.WRITE_POST.amount,
                type = PointType.WRITE_POST,
                refType = PointReferenceType.POST,
                refId = postId
            )
        }

        fun forWriteComment(userId: UserId, commentId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.WRITE_COMMENT.amount,
                type = PointType.WRITE_COMMENT,
                refType = PointReferenceType.COMMENT,
                refId = commentId
            )
        }

        fun forAttendance(userId: UserId, attendanceDate: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.ATTENDANCE.amount,
                type = PointType.ATTENDANCE,
                refType = PointReferenceType.ATTENDANCE,
                refId = attendanceDate
            )
        }

        fun forAttendanceStreak(userId: UserId, attendanceDate: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.ATTENDANCE_STREAK.amount,
                type = PointType.ATTENDANCE_STREAK,
                refType = PointReferenceType.ATTENDANCE,
                refId = attendanceDate
            )
        }

        fun forStudyCreate(userId: UserId, studyId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.STUDY_CREATE.amount,
                type = PointType.STUDY_CREATE,
                refType = PointReferenceType.STUDY,
                refId = studyId
            )
        }

        fun forStudyJoin(userId: UserId, studyId: Long): PointHistory {
            return create(
                userId = userId,
                amount = PointType.STUDY_JOIN.amount,
                type = PointType.STUDY_JOIN,
                refType = PointReferenceType.STUDY,
                refId = studyId
            )
        }

        fun forPurchase(userId: UserId, amount: Long, itemId: Long): PointHistory {
            return create(
                userId = userId,
                amount = amount,
                type = PointType.USE_SHOP,
                refType = PointReferenceType.SHOP_ITEM,
                refId = itemId
            )
        }

        fun forUpgradePurchase(userId: UserId, amount: Long, itemId: Long): PointHistory {
            return create(
                userId = userId,
                amount = amount,
                type = PointType.USE_SHOP,
                refType = PointReferenceType.UPGRADE_SHOP_ITEM,
                refId = itemId
            )
        }

        fun forAdminGiven(userId: UserId, amount: Long, adminId: UserId): PointHistory {
            return create(
                userId = userId,
                amount = amount,
                type = PointType.EVENT_ADMIN,
                refType = PointReferenceType.EVENT,
                refId = LocalDateTime.now()
                        .format(DateTimeFormatter.ofPattern("yyyyMMddHHmm"))
                        .toLong()
            )
        }

        private fun validate(userId: UserId, amount: Long, refId: Long) {
            if (userId <= 0) {
                throw BusinessException(PointErrorCode.INVALID_USER_ID)
            }
            if (amount <= 0L) {
                throw BusinessException(PointErrorCode.INVALID_AMOUNT)
            }
            if (refId <= 0) {
                throw BusinessException(PointErrorCode.INVALID_REF_ID)
            }
        }
    }
}