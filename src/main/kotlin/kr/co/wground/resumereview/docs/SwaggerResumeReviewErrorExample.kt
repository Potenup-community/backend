package kr.co.wground.resumereview.docs

object SwaggerResumeReviewErrorExample {

    const val INVALID_INPUT = """{
      "code":"INVALID_INPUT",
      "message":"요청값이 올바르지 않습니다.",
      "status":400,
      "errors":[{"field":"resumeReviewTitle","reason":"제목을 작성해주세요."}]
    }"""

    const val NOT_FOUND_REVIEW = """{
      "code":"RR-0001",
      "message":"해당 이력서 첨삭을 찾을 수 없습니다.",
      "status":404,
      "errors":[]
    }"""
}
