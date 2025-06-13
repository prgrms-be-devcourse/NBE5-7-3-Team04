package me.performancereservation.global.security.oauth.user

class NaverOAuth2UserInfo(attributes: Map<String, Any>) : OAuth2UserInfo(attributes) {
    private val response: Map<*, *>? = attributes["response"] as? Map<*, *>
    override val oauthId: String get() = response?.get("id") as? String ?: ""
    override val name: String get() = response?.get("name") as? String ?: ""
    override val email: String get() = response?.get("email") as? String ?: ""
}
