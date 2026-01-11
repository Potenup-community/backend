package kr.co.wground.user.docs


object UserSwaggerResponseExample {
    const val MY_INFO = """{
             "userId": 1,
             "name": "홍길동",
             "email": "gildong@gmail.com",
             "trackId": 1,
             "profileImageUrl": "/api/v1/users/profiles/1",
            "role": "MEMBER"
        }"""

    const val LOGIN_SUCCESS = """{
            "role": "MEMBER"
        }"""

    const val AUTH_STATUS = """{
            "isAuthenticated": true,
            "userId": 1,
            "role": "MEMBER"
        }"""

    const val USER_LIST = """{
            "content": [
                {
                    "userId": 1,
                    "name": "홍길동",
                    "email": "gildong@gmail.com",
                    "phoneNumber": "01012345678",
                    "trackName": "BE 1기",
                    "academicStatus": "ENROLLED",
                    "status": "ACCEPTED",
                    "role": "MEMBER"
                }
            ],
            "pageInfo": {
                "currentPage": 0,
                "pageSize": 20,
                "totalElements": 1,
                "totalPages": 1,
                "isFirst": true,
                "isLast": true
            }
        }"""

    const val USER_COUNT = """{
            "totalCount": 100,
            "signupSummary": {
                "pendingCount": 10,
                "approvedCount": 80,
                "rejectedCount": 10
            },
            "roleSummary": {
                "adminCount": 2,
                "userCount": 98
            },
            "statusSummary": {
                "activeCount": 90,
                "inactiveCount": 10
            },
            "academicSummary": {
                "enrolledCount": 50,
                "graduatedCount": 30,
                "leaveOfAbsenceCount": 20
            }
        }"""
}
