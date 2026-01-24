package kr.co.wground.study.docs

object StudySwaggerResponseExample {
         const val STUDY_DETAIL_RESPONSE = """
          {
           "id": 1,
           "scheduleId": 10,
           "scheduleName": "1차",
           "leaderId": 100,
           "name": "코틀린 정복 스터디",
           "description": "코틀린 기초부터 심화까지 함께 공부합니다.",
           "capacity": 6,
           "currentMemberCount": 3,
           "status": "RECRUITING",
           "budget": "FREE",
           "chatUrl": "https://open.kakao.com/...",
           "refUrl": "https://github.com/...",
           "tags": [
             "KOTLIN",
             "SPRING",
             "BACKEND"
           ],
           "createdAt": "2026-01-25T14:00:00",
           "updatedAt": "2026-01-25T14:00:00",
           "isRecruitmentClosed": false,
           "isLeader": true
         }
         """
             const val STUDY_SEARCH_RESPONSE = """
         {
           "content": [
             {
               "id": 1,
               "schedule": {
                  "id": 10,
                  "month": "1차",
                  "recruitStartDate": "2026-01-20T00:00:00",
                  "recruitEndDate": "2026-01-27T23:59:59",
                  "studyEndDate": "2026-02-28T23:59:59"
               },
               "leader": {
                  "id": 100,
                  "name": "홍길동",
                  "trackId": 3,
                  "trackName": "백엔드 4기",
                  "profileImageUrl": "https://..."
               },
               "name": "코틀린 정복 스터디",
               "description": "코틀린 기초부터 심화까지 함께 공부합니다.",
               "capacity": 6,
               "currentMemberCount": 3,
               "status": "RECRUITING",
               "budget": "FREE",
               "chatUrl": null,
               "refUrl": "https://github.com/...",
               "tags": [
                 "KOTLIN",
                 "SPRING"
               ],
               "createdAt": "2026-01-25T14:00:00",
               "updatedAt": "2026-01-25T14:00:00",
               "isRecruitmentClosed": false,
               "isLeader": false
             }
           ],
           "pageNumber": 0,
           "pageSize": 10,
           "hasNext": true
         }
         """

         const val STUDY_ID_RESPONSE = """
         {
             "studyId": 1
         }
         """

         const val RECRUITMENT_LIST_RESPONSE = """
         [
           {
             "id": 5,
             "studyId": 1,
             "studyName": "코틀린 정복 스터디",
             "trackName": "백엔드 4기",
             "userId": 101,
             "userName": "이순신",
             "appeal": "열심히 참여하겠습니다.",
             "status": "PENDING",
             "createdAt": "2026-01-26T09:00:00",
             "approvedAt": null
           },
           {
             "id": 6,
             "studyId": 1,
             "studyName": "코틀린 정복 스터디",
             "trackName": "백엔드 4기",
             "userId": 102,
             "userName": "장보고",
             "appeal": "코틀린 마스터가 되고 싶어요.",
             "status": "APPROVED",
             "createdAt": "2026-01-26T10:00:00",
             "approvedAt": "2026-01-27T10:00:00"
          }
        ]
        """

         const val SCHEDULE_CREATE_RESPONSE = """
        {
          "id": 15,
          "trackId": 3,
          "months": "FIRST"
        }
        """

         const val SCHEDULE_UPDATE_RESPONSE = """
        {
          "id": 15,
          "trackId": 3,
          "months": "SECOND"
        }
        """
     }