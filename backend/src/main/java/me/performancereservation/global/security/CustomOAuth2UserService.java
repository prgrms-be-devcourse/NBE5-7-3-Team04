package me.performancereservation.global.security;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.auth.entity.Auth;
import me.performancereservation.domain.auth.service.AuthService;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.domain.user.enums.Role;
import me.performancereservation.domain.user.service.UserService;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;


//소셜 인증 성공 시 소셜 유저 정보를 우리 서비스의 User/Auth와 연결(회원가입/로그인)
//소셜별로 내려주는 유저 정보 구조가 다르기 때문에 직접 커스텀 필요 -> 소셜 별 파싱 로직을 추상화하여 하나로 통일

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserService userService;
    private final AuthService authService;

    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        //소셜에서 준 유저 정보 가져오기
         OAuth2User oAuth2User = super.loadUser(userRequest);

        //provider(google, kakao, naver 등) 추출
        String provider= userRequest.getClientRegistration().getRegistrationId();

        //소셜별로 유저 정보 추출(소셜마다 제공 방식이 다르다.)
        Map<String, Object> attributes = oAuth2User.getAttributes();
        String oauthId, email, name;

        if ("google".equals(provider)) {
            oauthId = (String) attributes.get("sub");
            email = (String) attributes.get("email");
            name = (String) attributes.get("name");
        } else if ("kakao".equals(provider)) {
            Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");
            Map<String, Object> profile = (Map<String, Object>) kakaoAccount.get("profile");
            oauthId = String.valueOf(attributes.get("id"));
            email = (String) kakaoAccount.get("email");
            name = (String) profile.get("nickname");
        } else if ("naver".equals(provider)) {
            Map<String, Object> response = (Map<String, Object>) attributes.get("response");
            oauthId = (String) response.get("id");
            email = (String) response.get("email");
            name = (String) response.get("name");
        } else {
            throw new OAuth2AuthenticationException("지원하지 않는 소셜 로그인입니다.");
        }

        //소셜 계정이 이미 있는지 조회
        Auth auth;
        try {
            auth = authService.getUserByProviderAndOauthId(provider, oauthId);
        } catch (Exception e) { //없으면 회원가입
            User user = userService.registerUser(email, name, null, Role.USER);
            auth = authService.registerAuth(user.getId(), provider, oauthId);
        }

        //유저 정보 반환(principal)
        User user = userService.getUserById(auth.getUserId());

        return new CustomOAuth2User(user, attributes);
    }
}
