package kr.co.wground.post.docs

object SwaggerErrorExample {
    object InvalidArgument {
        const val VALIDATION = """{
            "code":"INVALID_INPUT",
            "message":"요청값이 올바르지 않습니다.",
            "status":400,
            "errors":[{"field":"title","reason":"제목을 작성해주세요."}]
            }"""
    }

    object NotFound {
        const val NOT_FOUND_POST = """{
            "code":"P-0001",
            "message":"해당 게시글을 찾을 수 없습니다.",
            "status":404,
            "errors":[]
            }"""

        const val NOT_FOUND_WRITER = """{
            "code":"P-0007",
            "message":"글쓴이를 찾을 수 없습니다.",
            "status":404,
            "errors":[]
            }"""
    }

    object FORBIDDEN {
        const val YOU_ARE_NOT_OWNER_THIS_POST = """{
            "code":"P-0005",
            "message":"해당 게시글의 주인이 아닙니다.",
            "status":403,
            "errors":[]
            }"""
    }
}
