package kr.co.wground.resumereview.docs

object SwaggerResumeReviewResponseExample {

    const val REVIEW_ACCEPTED = """{
      "resumeReviewId": 1,
      "status": "PROCESSING"
    }"""

    const val REVIEW_LIST = """{
      "contents": [
        {
          "resumeReviewId": 1,
          "userId": 10,
          "resumeReviewTitle": "백엔드 지원 이력서",
          "status": "COMPLETED"
        }
      ]
    }"""

    const val REVIEW_DETAIL = """{
      "resumeReviewId": 1,
      "resumeReviewTitle": "백엔드 지원 이력서",
      "resumeSections": {
        "summary": "저는 3년차 백엔드 개발자입니다.",
        "skills": "Java, Spring Boot",
        "experience": "MSA 프로젝트 경험",
        "education": "컴퓨터공학 학사",
        "projects": "결제 시스템 구축",
        "cert": "정보처리기사"
      },
      "resultJason": "{...LLM 결과 JSON...}",
      "completedAt": "2026-02-23T18:00:00",
      "status": "COMPLETED",
      "createdAt": "2026-02-23T17:50:00"
    }"""
}
