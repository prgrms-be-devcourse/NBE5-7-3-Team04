package me.performancereservation.global.security;

import me.performancereservation.domain.user.entitiy.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.core.user.OAuth2User;

import java.util.Collection;
import java.util.List;
import java.util.Map;

//인증된 사용자 정보를 Spring Security의 Principal로 감싸서 반환
//User 엔티티 전체를 노출하지 않고 필요한 정보만 노출

public class CustomOAuth2User implements OAuth2User {

    private final User user;
    private final Map<String, Object> attributes;

    public CustomOAuth2User(User user, Map<String, Object> attributes) {
        this.user = user;
        this.attributes = attributes;
    }

    @Override
    public Map<String, Object> getAttributes() { //소셜에서 내려준 원본 정보 반환(필요시)
        return attributes;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        //Spring Security의 권한 정보 반환(Role을 SimpleGrantedAuthority로 변환)
        return List.of(new SimpleGrantedAuthority(user.getRole().toString()));
    }

    @Override
    public String getName() {
        return user.getName(); // 고유 식별자 역할
    }

}
