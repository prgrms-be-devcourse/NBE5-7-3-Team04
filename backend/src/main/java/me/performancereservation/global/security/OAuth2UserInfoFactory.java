package me.performancereservation.global.security;

import java.util.Map;

//provider 값에 따라 구현체를 반환(서비스에서 그대로 해도 기능은 작동하지만 팩토리 패턴 적용)
public class OAuth2UserInfoFactory {
    public static OAuth2UserInfo getOAuth2UserInfo(String provider, final Map<String, Object> attributes) {
        return switch (provider) {
            case "google" -> new GoogleOAuth2UserInfo(attributes);
            case "kakao" -> new KakaoOAuth2UserInfo(attributes);
            case "naver" -> new NaverOAuth2UserInfo(attributes);
            default -> throw new IllegalArgumentException("Unknown provider: " + provider);
        };
    }
}
