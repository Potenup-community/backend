package kr.co.wground.reaction.docs

object SwaggerReactionResponseExample {
    const val POST_REACTION_STATS_RESPONSE = """{
        "postId": 1,
        "totalCount": 3,
        "summaries": {
            "HEART": {
                "count": 1,
                "reactedByMe": true
            },
            "SMILE": {
                "count": 1,
                "reactedByMe": true
            },
            "LIKE": {
                "count": 1,
                "reactedByMe": true
            }
        }
    }"""

    const val POST_REACTION_STATS_IN_BATCH_RESPONSE = """{
        "1": {
            "postId": 1,
            "totalCount": 3,
            "summaries": {
                "LIKE": {
                    "count": 1,
                    "reactedByMe": true
                },
                "HEART": {
                    "count": 1,
                    "reactedByMe": true
                },
                "SMILE": {
                    "count": 1,
                    "reactedByMe": true
                }
            }
        },
        "2": {
            "postId": 2,
            "totalCount": 5,
            "summaries": {
                "LIKE": {
                    "count": 2,
                    "reactedByMe": true
                },
                "HEART": {
                    "count": 1,
                    "reactedByMe": true
                },
                "SMILE": {
                    "count": 2,
                    "reactedByMe": true
                }
            }
        }
    }"""

    const val COMMENT_REACTION_STATS_IN_BATCH_RESPONSE = """{
        "1": {
            "commentId": 1,
            "totalCount": 1,
            "summaries": {
                "LIKE": {
                    "count": 1,
                    "reactedByMe": true
                }
            }
        },
        "2": {
            "commentId": 2,
            "totalCount": 3,
            "summaries": {
                "LIKE": {
                    "count": 3,
                    "reactedByMe": false
                }
            }
        }
    }"""
}