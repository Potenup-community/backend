package kr.co.wground.track.docs

object TrackSwaggerErrorExample {
    object NotFound {
        const val TRACK_NOT_FOUND = """{
                 "code":"T-0001",
                 "message":"해당 과정을 찾을 수 없습니다.",
                 "status":404,
                 "errors":[]
            }"""
    }

    object BadRequest {
        const val INVALID_DATE_RANGE = """{
                "code":"T-0002",
                "message":"시작 일자는 종료 일자이후가 될 수 없습니다.",
                "status":400,
                "errors":[]
            }"""

        const val TRACK_NAME_IS_BLANK = """{
                "code":"T-0003",
                "message":"트랙의 이름은 빈칸이 될 수 없습니다.",
                "status":400,
                "errors":[]
            }"""
    }
}