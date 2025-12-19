package kr.co.wground.reaction.exception

import kr.co.wground.exception.BusinessException
import kr.co.wground.reaction.exception.ReactionErrorCode.COMMENT_ID_IS_NEGATIVE
import kr.co.wground.reaction.exception.ReactionErrorCode.COMMENT_ID_IS_NULL
import kr.co.wground.reaction.exception.ReactionErrorCode.COMMENT_NOT_FOUND
import kr.co.wground.reaction.exception.ReactionErrorCode.POST_ID_IS_NEGATIVE
import kr.co.wground.reaction.exception.ReactionErrorCode.POST_ID_IS_NULL
import kr.co.wground.reaction.exception.ReactionErrorCode.POST_NOT_FOUND
import kr.co.wground.reaction.exception.ReactionErrorCode.REACTION_TYPE_IS_NULL
import kr.co.wground.reaction.exception.ReactionErrorCode.USER_ID_IS_NEGATIVE
import kr.co.wground.reaction.exception.ReactionErrorCode.USER_ID_IS_NULL

class ReactionException : BusinessException {

    constructor(
        errorCode: ReactionErrorCode
    ) : super(
        errorCode = errorCode
    )

    companion object {
        fun userIdIsNull() : ReactionException =
            ReactionException(USER_ID_IS_NULL)

        fun postIdIsNull() : ReactionException =
            ReactionException(POST_ID_IS_NULL)

        fun commentIdIsNull() : ReactionException =
            ReactionException(COMMENT_ID_IS_NULL)

        fun reactionTypeIsNull() : ReactionException =
            ReactionException(REACTION_TYPE_IS_NULL)

        fun userIdIsNegative() : ReactionException =
            ReactionException(USER_ID_IS_NEGATIVE)

        fun postIdIsNegative() : ReactionException =
            ReactionException(POST_ID_IS_NEGATIVE)

        fun commentIdIsNegative() : ReactionException =
            ReactionException(COMMENT_ID_IS_NEGATIVE)

        fun postNotFound() : ReactionException =
            ReactionException(POST_NOT_FOUND)

        fun commentNotFound() : ReactionException =
            ReactionException(COMMENT_NOT_FOUND)
    }
}