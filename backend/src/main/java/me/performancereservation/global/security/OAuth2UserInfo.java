package me.performancereservation.global.security;

import java.util.Map;

//소셜별로 내려주는 유저 정보 구조가 다르므로 공통 인터페이스로 표준화
public abstract class OAuth2UserInfo {
    protected Map<String, Object> attributes;

    public OAuth2UserInfo(Map<String, Object> attributes) {
        this.attributes = attributes;
    }

    public abstract String getName();
    public abstract String getEmail();
    public abstract String getProviderId();
}
