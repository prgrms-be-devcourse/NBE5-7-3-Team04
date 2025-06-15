package me.performancereservation.global.security.oauth.user

//소셜별로 내려주는 유저 정보 구조가 다르므로 공통 인터페이스로 표준화
abstract class OAuth2UserInfo(
    protected val attributes: Map<String, Any>
) {
    abstract val name: String?
    abstract val email: String?
    abstract val oauthId: String?
}
