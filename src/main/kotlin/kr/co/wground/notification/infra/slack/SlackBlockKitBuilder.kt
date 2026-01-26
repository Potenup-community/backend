package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageTemplate
import kr.co.wground.notification.infra.template.SlackTemplate
import org.springframework.stereotype.Component

@Component
class SlackBlockKitBuilder(
    private val messageTemplate: NotificationMessageTemplate<SlackTemplate>,
) {

    fun build(message: NotificationMessage): Map<String, Any> {
        val templateParams = buildTemplateParams(message)
        val template = messageTemplate.format(message.type, templateParams)

        return payload {
            header(template.header)
            divider()
            if (template.body.isNotBlank()) {
                section(template.body)
            }
            message.link?.let { button(template.buttonText, it) }
        }
    }

    private fun buildTemplateParams(message: NotificationMessage): Map<String, String> {
        val params = mutableMapOf<String, String>()
        params.putAll(message.metadata)
        return params
    }

    // DSL Builder
    private fun payload(init: BlockBuilder.() -> Unit): Map<String, Any> {
        val builder = BlockBuilder()
        builder.init()
        return mapOf("blocks" to builder.blocks)
    }

    private class BlockBuilder {
        val blocks = mutableListOf<Map<String, Any>>()

        fun header(text: String) {
            blocks.add(
                mapOf(
                    "type" to "header",
                    "text" to mapOf(
                        "type" to "plain_text",
                        "text" to text,
                        "emoji" to true
                    )
                )
            )
        }

        fun divider() {
            blocks.add(mapOf("type" to "divider"))
        }

        fun section(text: String) {
            blocks.add(
                mapOf(
                    "type" to "section",
                    "text" to mapOf(
                        "type" to "mrkdwn",
                        "text" to text
                    )
                )
            )
        }

        fun button(text: String, url: String, style: String = "primary") {
            blocks.add(
                mapOf(
                    "type" to "actions",
                    "elements" to listOf(
                        mapOf(
                            "type" to "button",
                            "text" to mapOf(
                                "type" to "plain_text",
                                "text" to text,
                                "emoji" to true
                            ),
                            "url" to url,
                            "style" to style
                        )
                    )
                )
            )
        }
    }
}
