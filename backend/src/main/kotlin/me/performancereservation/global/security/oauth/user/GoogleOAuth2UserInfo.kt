package me.performancereservation.global.security.oauth.user

class GoogleOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    override val name: String
        get() = attributes["name"] as String

    override val email: String
        get() = attributes["email"] as String

    override val oauthId: String
        get() = attributes["sub"] as String
}
