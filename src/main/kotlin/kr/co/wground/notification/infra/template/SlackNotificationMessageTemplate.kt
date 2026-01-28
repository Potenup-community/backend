package kr.co.wground.notification.infra.template

import kr.co.wground.exception.BusinessException
import kr.co.wground.notification.application.port.NotificationMessageTemplate
import kr.co.wground.notification.application.port.NotificationMessageType
import kr.co.wground.notification.exception.NotificationErrorCode
import org.springframework.stereotype.Component

/**
 * 슬랙 알림 메시지 템플릿
 *
 * [운영진 수정 가이드]
 * - header: 상단에 크게 표시되는 제목
 * - body: 본문 내용 ({title}, {count}, {posts} 등 플레이스홀더 사용 가능)
 * - buttonText: 하단 버튼에 표시되는 텍스트
 *
 * 플레이스홀더 목록:
 * - {title}: 게시글/공지사항 제목
 * - {count}: 새 글 개수
 * - {posts}: 새 글 목록
 * - {link}: 바로가기 링크 (버튼에 자동 적용)
 */
@Component
class SlackNotificationMessageTemplate : NotificationMessageTemplate<SlackTemplate> {

    private val templates = mapOf(
        NotificationMessageType.ANNOUNCEMENT to SlackTemplate(
            header = "🔔 새 공지사항이 등록되었습니다",
            body = "📌 *{title}*",
            buttonText = "공지사항 바로가기"
        ),

        NotificationMessageType.NEW_POSTS_SUMMARY to SlackTemplate(
            header = "📊 새 글 {count}건이 작성되었습니다",
            body = "{posts}",
            buttonText = "커뮤니티 바로가기"
        ),
    )

    override fun getTemplate(type: NotificationMessageType): SlackTemplate {
        return templates[type] ?: throw BusinessException(NotificationErrorCode.TEMPLATE_NOT_FOUND)
    }

    override fun format(type: NotificationMessageType, params: Map<String, String>): SlackTemplate {
        val template = getTemplate(type)
        return SlackTemplate(
            header = replacePlaceholders(template.header, params),
            body = replacePlaceholders(template.body, params),
            buttonText = template.buttonText,
        )
    }

    private fun replacePlaceholders(text: String, params: Map<String, String>): String {
        var result = text
        params.forEach { (key, value) ->
            result = result.replace("{$key}", value)
        }
        return result
    }
}

data class SlackTemplate(
    val header: String,
    val body: String,
    val buttonText: String,
)
