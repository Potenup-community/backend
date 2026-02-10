package kr.co.wground.notification.infra.slack

import kr.co.wground.notification.application.port.NotificationMessage
import kr.co.wground.notification.application.port.NotificationMessageTemplate
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.BLOCK_TYPE_ACTIONS
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.BLOCK_TYPE_BUTTON
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.BLOCK_TYPE_DIVIDER
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.BLOCK_TYPE_HEADER
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.BLOCK_TYPE_SECTION
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_BLOCKS
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_ELEMENTS
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_EMOJI
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_STYLE
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_TEXT
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_TYPE
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.KEY_URL
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.STYLE_PRIMARY
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.TEXT_TYPE_MRKDWN
import kr.co.wground.notification.infra.slack.SlackBlockKitConstants.TEXT_TYPE_PLAIN
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
        return mapOf(KEY_BLOCKS to builder.blocks)
    }

    private class BlockBuilder {
        val blocks = mutableListOf<Map<String, Any>>()

        fun header(text: String) {
            blocks.add(
                mapOf(
                    KEY_TYPE to BLOCK_TYPE_HEADER,
                    KEY_TEXT to mapOf(
                        KEY_TYPE to TEXT_TYPE_PLAIN,
                        KEY_TEXT to text,
                        KEY_EMOJI to true
                    )
                )
            )
        }

        fun divider() {
            blocks.add(mapOf(KEY_TYPE to BLOCK_TYPE_DIVIDER))
        }

        fun section(text: String) {
            blocks.add(
                mapOf(
                    KEY_TYPE to BLOCK_TYPE_SECTION,
                    KEY_TEXT to mapOf(
                        KEY_TYPE to TEXT_TYPE_MRKDWN,
                        KEY_TEXT to text
                    )
                )
            )
        }

        fun button(text: String, url: String, style: String = STYLE_PRIMARY) {
            blocks.add(
                mapOf(
                    KEY_TYPE to BLOCK_TYPE_ACTIONS,
                    KEY_ELEMENTS to listOf(
                        mapOf(
                            KEY_TYPE to BLOCK_TYPE_BUTTON,
                            KEY_TEXT to mapOf(
                                KEY_TYPE to TEXT_TYPE_PLAIN,
                                KEY_TEXT to text,
                                KEY_EMOJI to true
                            ),
                            KEY_URL to url,
                            KEY_STYLE to style
                        )
                    )
                )
            )
        }
    }
}
