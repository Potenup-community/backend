package kr.co.wground.user.docs

object UserSwaggerErrorExample {
    object NotFound {
        const val USER_NOT_FOUND = """{
                  "code":"U-0006",
                  "message":"유저를 찾을 수 없습니다.",
                  "errors":[]
             }"""

        const val REQUEST_SIGNUP_NOT_FOUND = """{
                 "code":"U-0001",
                 "message":"해당 가입요청을 찾을 수 없습니다.",
                 "errors":[]
             }"""

        const val PROFILE_NOT_FOUND = """{
                 "code":"U-0016",
                 "message":"해당 유저의 프로필 정보가 존재하지 않습니다.",
                 "errors":[]
             }"""

        const val REFRESH_TOKEN_NOT_FOUND = """{
                 "code":"U-0012",
                 "message":"리프레시 토큰을 찾을수 없습니다.",
                 "errors":[]
             }"""
    }

    object BadRequest {
        const val INACTIVE_USER = """{
                 "code":"U-0008",
                 "message":"유저가 활성화 되지않았습니다.",
                 "errors":[]
             }"""

        const val ALREADY_SIGNED_USER = """{
                 "code":"U-0005",
                 "message":"이미 가입된 유저 입니다.",
                 "errors":[]
             }"""

        const val REQUEST_SIGNUP_ALREADY_EXISTED = """{
                 "code":"U-0002",
                 "message":"이미 가입 요청한 유저 입니다.",
                 "errors":[]
             }"""

        const val DUPLICATED_PHONE_NUMBER = """{
                 "code":"U-0018",
                 "message":"해당 전화번호는 이미 등록되어 있습니다.",
                 "errors":[]
             }"""

        const val PAGE_NUMBER_MIN_ERROR = """{
                 "code":"U-0014",
                 "message":"최소 페이지 수에 해당하지 않습니다.",
                 "errors":[]
             }"""

        const val PAGE_NUMBER_IS_OVER_TOTAL_PAGE = """{
                 "code":"U-0013",
                 "message":"최대 페이지 수를 넘었습니다.",
                 "errors":[]
             }"""

        const val CANT_REQUEST_NEXT_PAGE_IN_ZERO_ELEMENT = """{
                 "code":"U-0015",
                 "message":"해당 속성에 해당되는 유저가 없습니다. 다음 페이지는 요청 될 수 없습니다.",
                 "errors":[]
             }"""
    }

    object Unauthorized {
        const val INVALID_ACCESS_TOKEN = """{
                 "code":"U-0011",
                 "message":"액세스 토큰이 유효하지 않습니다.",
                 "errors":[]
             }"""

        const val INVALID_REFRESH_TOKEN = """{
                 "code":"U-0009",
                 "message":"리프레시 토큰이 유효하지 않습니다.",
                 "errors":[]
             }"""

        const val TOKEN_EXPIRED = """{
                "code":"U-0010",
                "message":"엑세스 토큰이 만료되었습니다.",
                "errors":[]
            }"""
    }
}