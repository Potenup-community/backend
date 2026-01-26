package kr.co.wground.comment.docs

object CommentSwaggerResponseExample {

    const val COMMENTS = """
        {
          "contents": [
            {
              "commentId": 1,
              "content": "test content",
              "author": {
                "userId": 1,
                "name": "홍길동",
                "trackName": "BE 트랙 1기",
                "profileImageUrl": "/api/v1/users/profiles/1"
              },
              "createdAt": "2026-01-07T20:49:41.19446",
              "commentReactionStats": {
                "commentId": 1,
                "totalCount": 1,
                "summaries": {
                  "LIKE": {
                    "count": 1,
                    "reactedByMe": true
                  }
                }
              },
              "isDeleted": false,
              "replies": [
                {
                  "commentId": 2,
                  "content": "test sub content",
                  "author": {
                    "userId": 2,
                    "name": "김철수",
                    "trackName": "FE 트랙 1기",
                    "profileImageUrl": "/api/v1/users/profiles/2"
                  },
                  "createdAt": "2026-01-09T01:54:50.454014",
                  "commentReactionStats": {
                    "commentId": 2,
                    "totalCount": 0,
                    "summaries": {}
                  },
                  "isDeleted": false,
                  "replies": []
                }
              ]
            }
          ]
        }
    """

    const val MY_COMMENTS = """
        {
          "contents": [
            {
              "commentId": 2,
              "postId": 1,
              "content": "test sub content",
              "author": {
                "userId": 1,
                "name": "홍길동",
                "trackName": "BE 트랙 1기",
                "profileImageUrl": "/api/v1/users/profiles/1"
              },
              "createdAt": "2026-01-11T01:38:02.647253",
              "isDeleted": false
            },
            {
              "commentId": 1,
              "postId": 1,
              "content": "test content",
              "author": {
                "userId": 2,
                "name": "김철수",
                "trackName": "FE 트랙 1기",
                "profileImageUrl": "/api/v1/users/profiles/2"
              },
              "createdAt": "2026-01-10T01:54:50.454014",
              "isDeleted": false
            }
          ],
          "hasNext": false,
          "nextPage": null
        }
    """

    const val LIKED_COMMENTS = """
        {
          "contents": [
            {
              "commentId": 1,
              "postId": 1,
              "content": "test content",
              "author": {
                "userId": 1,
                "name": "홍길동",
                "trackName": "BE 트랙 1기",
                "profileImageUrl": "/api/v1/users/profiles/1"
              },
              "createdAt": "2026-01-07T20:49:41.19446",
              "likedAt": "2026-01-14T23:14:11.146097",
              "commentReactionStats": {
                "commentId": 1,
                "totalCount": 1,
                "summaries": {
                  "LIKE": {
                    "count": 1,
                    "reactedByMe": true
                  }
                }
              },
              "isDeleted": false
            }
          ],
          "hasNext": false,
          "nextPage": null
        }
    """
}
