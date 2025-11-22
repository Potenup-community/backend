package kr.co.wground.global.auth

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import kr.co.wground.exception.BusinessException
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Component
import java.util.Collections

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
        } catch (e: Exception) {
            throw BusinessException(AuthErrorCode.INVALID_TOKEN)
        }

        return googleIdToken?.payload?.email ?: throw BusinessException(AuthErrorCode.INVALID_TOKEN)
    }
}