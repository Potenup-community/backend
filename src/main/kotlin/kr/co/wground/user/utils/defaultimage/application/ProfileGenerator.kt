package kr.co.wground.user.utils.defaultimage.application

import kr.co.wground.global.common.UserId
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarConstants
import kr.co.wground.user.utils.defaultimage.application.constant.AvatarPalette
import org.springframework.stereotype.Component
import java.util.zip.CRC32
import kotlin.math.abs

@Component
class ProfileGenerator {

    fun generateSvg(userId: UserId, name: String, email: String, size: Int): String {
        val hash = getHash(userId, email, name)
        val palette = AvatarPalette.fromHash(hash)

        val color1 = palette.colors[(abs(hash) % palette.colors.size).toInt()]
        val color2 = palette.colors[(abs(hash / palette.colors.size) % palette.colors.size).toInt()]

        val rotate = hash % 360
        val translateX = (hash % AvatarConstants.TRANSLATE_RANGE_X) - AvatarConstants.TRANSLATE_OFFSET_X
        val translateY = (hash % AvatarConstants.TRANSLATE_RANGE_Y) - AvatarConstants.TRANSLATE_OFFSET_Y

        return """
            <svg viewBox="0 0 ${AvatarConstants.VIEWBOX_SIZE} ${AvatarConstants.VIEWBOX_SIZE}" fill="none" xmlns="http://www.w3.org/2000/svg" width="$size" height="$size">
                <mask id="mask__beam" maskUnits="userSpaceOnUse" x="0" y="0" width="${AvatarConstants.VIEWBOX_SIZE}" height="${AvatarConstants.VIEWBOX_SIZE}">
                    <rect width="${AvatarConstants.VIEWBOX_SIZE}" height="${AvatarConstants.VIEWBOX_SIZE}" rx="${AvatarConstants.DEFAULT_RADIUS}" fill="white" />
                </mask>
                <g mask="url(#mask__beam)">
                    <rect width="${AvatarConstants.VIEWBOX_SIZE}" height="${AvatarConstants.VIEWBOX_SIZE}" fill="$color1" />
                    <rect x="0" y="0" width="${AvatarConstants.VIEWBOX_SIZE}" height="${AvatarConstants.VIEWBOX_SIZE}" 
                          transform="translate($translateX $translateY) rotate($rotate ${AvatarConstants.CENTER_COORD} ${AvatarConstants.CENTER_COORD}) scale(1.2)" fill="$color2" rx="${AvatarConstants.VIEWBOX_SIZE}" />
                    <g transform="translate(${translateX / 2} ${translateY / 2}) rotate($rotate ${AvatarConstants.CENTER_COORD} ${AvatarConstants.CENTER_COORD})">
                        <path d="M13,19 a1,0.75 0 0,0 10,0" fill="white" />
                        <rect x="11" y="14" width="1.5" height="2" rx="1" fill="white" />
                        <rect x="23" y="14" width="1.5" height="2" rx="1" fill="white" />
                    </g>
                </g>
            </svg>
        """.trimIndent()
    }

    private fun getHash(userId: UserId, email: String, name: String): Long {
        val input = "$userId:$email:$name"
        return CRC32().apply { update(input.toByteArray()) }.value
    }
}
