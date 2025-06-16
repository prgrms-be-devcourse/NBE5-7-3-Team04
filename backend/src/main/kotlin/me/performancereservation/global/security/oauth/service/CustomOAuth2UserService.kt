package me.performancereservation.global.security.oauth.service

import me.performancereservation.domain.auth.entity.Auth
import me.performancereservation.domain.auth.service.AuthService
import me.performancereservation.domain.user.entitiy.User
import me.performancereservation.domain.user.enums.Role
import me.performancereservation.domain.user.service.UserService
import me.performancereservation.domain.user.service.UserTransactionalService
import me.performancereservation.global.security.oauth.user.CustomOAuth2User
import me.performancereservation.global.security.oauth.user.OAuth2UserInfo
import me.performancereservation.global.security.oauth.user.OAuth2UserInfoFactory
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest
import org.springframework.security.oauth2.core.OAuth2AuthenticationException
import org.springframework.security.oauth2.core.user.OAuth2User
import org.springframework.stereotype.Service

//소셜 인증 성공 시 소셜 유저 정보를 우리 서비스의 User/Auth와 연결(회원가입/로그인)
//소셜별로 내려주는 유저 정보 구조가 다르기 때문에 직접 커스텀 필요 -> 소셜 별 파싱 로직을 추상화하여 하나로 통일
@Service
class CustomOAuth2UserService(
    private val userService: UserService,
    private val authService: AuthService,
    private val userTransactionalService: UserTransactionalService
) : DefaultOAuth2UserService() {

    override fun loadUser(userRequest: OAuth2UserRequest): OAuth2User {
        //소셜에서 준 유저 정보 가져오기
        val oAuth2User = super.loadUser(userRequest)

        //provider(google, kakao, naver 등) 추출
        val provider = userRequest.clientRegistration.registrationId

        //소셜별로 유저 정보 추출(소셜마다 제공 방식이 다르다.)
        //초기 if문을 통해 구현했지만 통합 인터페이스를 만들고 소셜별로 메소드를 분리, 팩토리 패턴으로 구현
        val userInfo: OAuth2UserInfo = OAuth2UserInfoFactory.getOAuth2UserInfo(provider, oAuth2User.attributes)

        //소셜 계정이 이미 있는지 조회
        var auth: Auth
        var isExist = true
        try {
            auth = authService.getUserByProviderAndOauthId(provider, userInfo.oauthId)
        } catch (e: Exception) { //없으면 회원가입
            isExist = false
            val user: User = userTransactionalService.registerUser(userInfo.email, userInfo.name, null.toString(), Role.USER)

            auth = authService.registerAuth(user.id!!, provider, userInfo.oauthId)
        }

        //유저 정보 반환(principal) Auth 정보에서 유저 id를 꺼내 실제 유저를 조회
        val user = userService.getUserById(auth.userId)

        // Attribute에 exist 정보 추가
        val attributes: MutableMap<String, Any> = HashMap(oAuth2User.attributes)
        attributes["exist"] = isExist

        //Spring Security는 리턴된 객체를 세션(Authentication)에 저장해서 인증 정보를 유지
        return CustomOAuth2User(user, attributes)
    }
}

