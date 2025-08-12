package lev.learn.sandbox.auth.service.model

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

@Serializable
data class TokenResponse(
    @SerialName("access_token")
    val accessToken: String,

    @SerialName("expires_in")
    val expiresIn: Long,

    @SerialName("refresh_expires_in")
    val refreshExpiresIn: Long,

    @SerialName("refresh_token")
    val refreshToken: String,

    @SerialName("token_type")
    val tokenType: String
)