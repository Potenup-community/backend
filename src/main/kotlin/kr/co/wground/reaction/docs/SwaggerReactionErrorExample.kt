package kr.co.wground.reaction.docs

object SwaggerReactionErrorExample {
    object BadRequest {
        const val NOT_SUPPORTED_TARGET_TYPE = """{
            "code": "V-0001",
            "message": "요청 값이 올바르지 않습니다.",
            "errors": [
                {
                    "field": "targetType",
                    "reason": "'NOTYPE'은(는) 유효한 값이 아닙니다."
                }
            ]
        }"""

        const val NOT_SUPPORTED_REACTION_TYPE = """{
            "code": "V-0001",
            "message": "요청 값이 올바르지 않습니다.",
            "errors": [
                {
                    "field": "reactionType",
                    "reason": "'NOTYPE'은(는) 유효한 값이 아닙니다."
                }
            ]
        }"""

        const val INVALID_TARGET_TYPE = """{
            "code": "V-0001",
            "message": "요청 값이 올바르지 않습니다.",
            "errors": [
                {
                    "field": "targetId",
                    "reason": "대상의 id 는 0 또는 음수일 수 없습니다."
                }
            ]
        }"""

        const val INVALID_POST_ID_LIST_SIZE = """{
            "code": "V-0001",
            "message": "요청 값이 올바르지 않습니다.",
            "errors": [
                {
                    "field": "postIds",
                    "reason": "postId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다."
                }
            ]
        }"""

        const val INVALID_COMMENT_ID_LIST_SIZE = """{
            "code": "V-0001",
            "message": "요청 값이 올바르지 않습니다.",
            "errors": [
                {
                    "field": "commentIds",
                    "reason": "commentId 집합이 너무 작거나 큽니다. 1 이상 50 이하여야 합니다."
                }
            ]
        }"""
    }

    object NotFound {
        const val POST_NOT_FOUND = """{
            "code": "PR-0008",
            "message": "반응할 게시글을 찾을 수 없습니다.",
            "errors": []
        }"""

        const val COMMENT_NOT_FOUND = """{
            "code": "PR-0009",
            "message": "반응할 댓글을 찾을 수 없습니다.",
            "errors": []
        }"""
    }
}