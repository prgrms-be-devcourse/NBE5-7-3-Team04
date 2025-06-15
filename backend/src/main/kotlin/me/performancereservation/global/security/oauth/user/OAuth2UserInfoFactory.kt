package me.performancereservation.global.security.oauth.user

import me.performancereservation.global.exception.AppException
import me.performancereservation.global.exception.ErrorCode
import me.performancereservation.global.exception.ErrorType

//provider 값에 따라 구현체를 반환(서비스에서 그대로 해도 기능은 작동하지만 팩토리 패턴 적용)
object OAuth2UserInfoFactory {
    fun getOAuth2UserInfo(provider: String, attributes: Map<String, Any>): OAuth2UserInfo {
        return when (provider) {
            "google" -> GoogleOAuth2UserInfo(attributes)
            "kakao" -> KakaoOAuth2UserInfo(attributes)
            "naver" -> NaverOAuth2UserInfo(attributes)
            else -> throw ErrorCode.INVALID_PROVIDER.serviceException()
        }
    }
}
