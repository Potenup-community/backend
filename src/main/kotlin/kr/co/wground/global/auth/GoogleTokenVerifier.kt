package kr.co.wground.global.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import io.jsonwebtoken.io.IOException
import kr.co.wground.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.security.GeneralSecurityException
import java.util.*

@Component
class GoogleTokenVerifier(
    @Value("\${google.auth.client-id}")
    private val clientId: String
) {
    fun verify(idToken: String): String {
        val verifier = GoogleIdTokenVerifier.Builder(NetHttpTransport(), GsonFactory())
            .setAudience(Collections.singletonList(clientId))
            .build()

        val googleIdToken: GoogleIdToken? = try {
            verifier.verify(idToken)
        } catch (e: IllegalArgumentException) {
            throw BusinessException(AuthErrorCode.MALFORMED_TOKEN)

        } catch (e: IOException) {
            throw BusinessException(AuthErrorCode.GOOGLE_SERVER_ERROR)

        } catch (e: GeneralSecurityException) {
            throw BusinessException(AuthErrorCode.INVALID_SIGNATURE)
        }

        if (googleIdToken == null) {
            throw BusinessException(AuthErrorCode.TOKEN_EXPIRED_OR_INVALID)
        }

        return googleIdToken?.payload?.email ?: throw BusinessException(AuthErrorCode.TOKEN_HAS_NOT_VALID_EMAIL)
    }
}
