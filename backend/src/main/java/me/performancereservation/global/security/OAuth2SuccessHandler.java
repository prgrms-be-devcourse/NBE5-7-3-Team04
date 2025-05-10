package me.performancereservation.global.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;

@Configuration
@RequiredArgsConstructor
public class OAuth2SuccessHandler implements AuthenticationSuccessHandler {

    private final JwtTokenProvider jwtTokenProvider;

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

        boolean isExist = Boolean.TRUE.equals(oAuth2User.getAttribute("exist"));


        if (isExist) { //회원이면 로그인 하고 콜백 페이지로
            String accessToken = jwtTokenProvider.createAccessToken(user);

            String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
                    .queryParam("accessToken", accessToken)
                            .toUriString();

            response.sendRedirect(redirectUrl);
        } else { //아니면 회원가입 유도
            String redirectUrl = UriComponentsBuilder.fromUriString(signupUrl)
                    .toUriString();
            response.sendRedirect(redirectUrl);

        }
    }
}
