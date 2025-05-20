package me.performancereservation.global.security.oauth.handler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import me.performancereservation.global.storage.redis.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;

    @Value("${spring.oauth2.url.callback}")
    private String callbackUrl;

    @Value("${spring.oauth2.url.sign-up}")
    private String signupUrl;


    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {

        CustomOAuth2User oAuth2User = (CustomOAuth2User) authentication.getPrincipal();

        User user = oAuth2User.getUser();

        String accessToken = jwtTokenProvider.createAccessToken(user);
        String refreshToken = jwtTokenProvider.createRefreshToken(user);

        //리프레쉬 토큰 레디스에 저장
        refreshTokenService.saveRefreshToken(user.getId(), refreshToken,jwtTokenProvider.getRefreshExpiration());

        boolean isExist = Boolean.TRUE.equals(oAuth2User.getAttribute("exist"));

        Object existAttr = oAuth2User.getAttribute("exist");

        String redirectUrl;
        if (isExist) { //회원이면 로그인 하고 콜백 페이지로
            redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .toUriString();
        } else { //아니면 회원가입 유도
            redirectUrl = UriComponentsBuilder.fromUriString(signupUrl)
                    .queryParam("accessToken", accessToken)
                    .queryParam("refreshToken", refreshToken)
                    .toUriString();
        }
        response.sendRedirect(redirectUrl);
    }
}
