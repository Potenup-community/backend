package kr.co.wground.study.docs

object StudySwaggerResponseExample {
    const val STUDY_DETAIL_RESPONSE =
        """
        {
            "id": 1,
            "name": "코틀린 정복 스터디",
            "description": "코틀린 기초부터 심화까지 함께 공부합니다.",
            "capacity": 6,
            "currentMemberCount": 3,
            "status": "RECRUITING",
            "budget": "MEAL",
            "budgetExplain": "피자먹을래요",
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
            "isLeader": true,
            "isParticipant": true,
            "participants": [
                {
                    "id": 1,
                    "name": "홍길동",
                    "trackId": 2,
                    "trackName": "BE 트랙 1기",
                    "joinedAt": "2026-01-27T15:32:21",
                    "profileImageUrl": "/...",
                    "items": [
                        {
                          "itemType": "PET",
                          "imageUrl": "/api/v1/shop/items/2/image"
                        },
                        {
                          "itemType": "FRAME",
                          "imageUrl": "/api/v1/shop/items/3/image"
                        },
                        {
                          "itemType": "BADGE",
                          "imageUrl": "/api/v1/shop/items/5/image"
                        }
                    ]
                },
                {
                    ...
                }
            ],
            "schedule": {
                "id": 3,
                "month": "1",
                "recruitmentStartDate": "2026-01-24T14:00:00",
                "recruitEndDate": "2026-01-27T14:00:00",
                "studyEndDate": "2026-02-25T14:00:00"
            },
            "leader": {
                {
                    "id": 1,
                    "name": "홍길동",
                    "trackId": 2,
                    "trackName": "BE 트랙 1기",
                    "joinedAt": "2026-01-27T15:32:21",
                    "profileImageUrl": "/..."
                },
            }
        }
         """

    const val STUDY_SEARCH_RESPONSE = """
         {
           "content": [
             {
               "id": 1,
               "name": "코틀린 정복 스터디",
               "description": "코틀린 기초부터 심화까지 함께 공부합니다.",
               "capacity": 6,
               "currentMemberCount": 3,
               "status": "RECRUITING",
               "chatUrl": null,
               "tags": [
                 "KOTLIN",
                 "SPRING"
               ],
               "createdAt": "2026-01-25T14:00:00",
               "updatedAt": "2026-01-25T14:00:00",
               "isLeader": false,
               "isParticipant": true,
               "leader": {
                  "id": 100,
                  "name": "홍길동",
                  "trackId": 3,
                  "trackName": "백엔드 4기",
                  "profileImageUrl": "https://...",
                  "items": [
                    {
                      "itemType": "FRAME",
                      "imageUrl": "/api/v1/shop/items/3/image"
                    },
                    {
                      "itemType": "BADGE",
                      "imageUrl": "/api/v1/shop/items/5/image"
                    }
                  ]
               },
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

    const val STUDY_REPORT_ID_RESPONSE = """
         {
             "reportId": 12
         }
         """

    const val STUDY_REPORT_SUBMISSION_STATUS_RESPONSE = """
         {
             "hasReport": true,
             "status": "RESUBMITTED",
             "submittedAt": "2026-02-18T15:00:00",
             "lastModifiedAt": "2026-02-18T15:10:00"
         }
         """

    const val STUDY_REPORT_APPROVAL_HISTORY_RESPONSE = """
         [
             {
                 "action": "RESUBMIT",
                 "actorId": 101,
                 "reason": null,
                 "timestamp": "2026-02-18T15:10:00"
             },
             {
                 "action": "REJECT",
                 "actorId": 1,
                 "reason": "주차별 활동 근거를 조금 더 구체적으로 작성해 주세요.",
                 "timestamp": "2026-02-18T14:30:00"
             }
         ]
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
             "createdAt": "2026-01-26T09:00:00",
             "items": []
           },
           {
             "id": 6,
             "studyId": 1,
             "studyName": "코틀린 정복 스터디",
             "trackName": "백엔드 4기",
             "userId": 102,
             "userName": "장보고",
             "createdAt": "2026-01-26T10:00:00",
             "items": [
                    {
                      "itemType": "PET",
                      "imageUrl": "/api/v1/shop/items/2/image"
                    },
                    {
                      "itemType": "FRAME",
                      "imageUrl": "/api/v1/shop/items/3/image"
                    },
                    {
                      "itemType": "BADGE",
                      "imageUrl": "/api/v1/shop/items/5/image"
                    }
                  ]
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

    const val SCHEDULE_LIST_QUERY_RESPONSE = """
       {
       "content":[
            {
            "scheduleId": 15,
            "months": "1"
            },
            {
            "scheduleId": 16,
            "months": "2"
            },
            {
            "scheduleId": 17,
            "months": "3"
            },
            {
            "scheduleId": 18,
            "months": "4"
            },
            {
            "scheduleId": 19,
            "months": "5"
            }
       ]
       }
    """

    const val STUDY_SCHEDULE_RESPONSE = """
        {
        "id": 12,
        "trackId": 2,
        "months": "FIRST",
        "monthName": "1",
        "recruitStartDate": "2025-01-29T10:14:48",
        "recruitEndDate": "2027-01-27T10:14:45",
        "studyEndDate": "2028-01-27T10:14:56"
    }
    """

    const val SCHEDULE_OF_TRACK_IDS =
        """
        {
          "2": [
            {
              "id": 3,
              "trackId": 2,
              "months": "1",
              "monthName": "First",
              "recruitStartDate": "2026-02-01T00:00:00",
              "recruitEndDate": "2026-02-14T23:59:59",
              "studyEndDate": "2026-03-31T23:59:59"
            },
            {
              "id": 7,
              "trackId": 2,
              "months": "2",
              "monthName": "Second",
              "recruitStartDate": "2026-03-01T00:00:00",
              "recruitEndDate": "2026-03-14T23:59:59",
              "studyEndDate": "2026-04-31T23:59:59"
            }
            ...
          ],
          "3": [
            ...
          ]
        }
        """
}
