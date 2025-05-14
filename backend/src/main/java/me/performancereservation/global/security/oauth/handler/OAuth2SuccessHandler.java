package me.performancereservation.global.security.oauth.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.user.entitiy.User;
import me.performancereservation.global.security.jwt.JwtTokenProvider;
import me.performancereservation.global.security.oauth.user.CustomOAuth2User;
import me.performancereservation.global.storage.redis.RefreshTokenService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;
import java.util.Map;

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
        //테스트를 위해 토큰을 json 방식으로 반환, 나중에 리다이렉션 방식으로 재수정 예정.
        response.setStatus(HttpServletResponse.SC_OK);
        response.setContentType("application/json; charset=utf-8");
        response.setCharacterEncoding("UTF-8");

        Map<String, String> tokenMap = Map.of("accessToken", accessToken, "refreshToken", refreshToken);
        ObjectMapper objectMapper = new ObjectMapper();
        String json = objectMapper.writeValueAsString(tokenMap);

        response.getWriter().write(json);


//        boolean isExist = Boolean.TRUE.equals(oAuth2User.getAttribute("exist"));
//
//        if (isExist) { //회원이면 로그인 하고 콜백 페이지로
//            String accessToken = jwtTokenProvider.createAccessToken(user);
//            String redirectUrl = UriComponentsBuilder.fromUriString(callbackUrl)
//                    .queryParam("accessToken", accessToken)
//                    .queryParam("refreshToken", refreshToken)
//                            .toUriString();
//            response.sendRedirect(redirectUrl);
//        } else { //아니면 회원가입 유도
//            String redirectUrl = UriComponentsBuilder.fromUriString(signupUrl)
//                    .toUriString();
//            response.sendRedirect(redirectUrl);
//        }
    }
}
