package kr.co.wground.comment.docs

object CommentSwaggerErrorExample {
    object BadRequest {
        const val EMPTY_CONTENT = """{
          "code":"C-0001",
          "message":"댓글 내용을 입력해주세요.",
          "errors":[]
        }"""

        const val INVALID_REPLY = """{
          "code":"C-0003",
          "message":"대댓글을 작성할 수 없습니다.",
          "errors":[]
        }"""

        const val INVALID_INPUT = """{
          "code":"C-0007",
          "message":"댓글 입력값이 올바르지 않습니다.",
          "errors":[{"field":"content","reason":"댓글은 2000자까지 작성할 수 있습니다."}]
        }"""
    }

    object NotFound {
        const val COMMENT = """{
          "code":"C-0004",
          "message":"댓글을 찾을 수 없습니다.",
          "errors":[]
        }"""

        const val COMMENT_PARENT = """{
          "code":"C-0005",
          "message":"댓글 부모 ID를 찾을 수 없습니다.",
          "errors":[]
        }"""

        const val TARGET_POST = """{
          "code":"C-0008",
          "message":"대상 게시글을 찾을 수 없습니다.",
          "errors":[]
        }"""
    }

    object Forbidden {
        const val NOT_WRITER = """{
          "code":"C-0006",
          "message":"댓글 작성자가 아닙니다.",
          "errors":[]
        }"""
    }

    object Common {
        const val INVALID_INPUT_CONTENT = """{
          "code":"V-0001",
          "message":"요청 값이 올바르지 않습니다.",
          "errors":[{"field":"content","reason":"필수 값입니다. 누락되었거나 null일 수 없습니다."}]
        }"""

        const val INVALID_INPUT_POST_ID = """{
          "code":"V-0001",
          "message":"요청 값이 올바르지 않습니다.",
          "errors":[] 
        }"""
    }
}
