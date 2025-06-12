package me.performancereservation.global.security.oauth.user

class KakaoOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override val oauthId: String
        get() = attributes["id"].toString()

    override val name: String
        get() =
        ((attributes["kakao_account"] as? Map<*, *>)?.get("profile") as? Map<*, *>)?.get("nickname") as? String ?: ""

    override val email: String get() =
        (attributes["kakao_account"] as? Map<*, *>)?.get("email") as? String ?: ""
}