package me.performancereservation.domain.auth.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.auth.entity.Auth;
import me.performancereservation.domain.auth.repository.AuthRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AuthRepository authRepository;

    public Auth registerAuth(Long userId, String provider, String oauthId) {
        if(authRepository.findByProviderAndOauthId(provider, oauthId).isPresent()) {
            throw ErrorCode.DUPLICATE_AUTH_SOCIAL.serviceException(); //이미 그 소셜 로그인으로 가입한 유저
        }
        //아니면 새로 가입 시켜
        Auth auth = Auth.builder()
                .userId(userId)
                .provider(provider)
                .oauthId(oauthId)
                .build();
        return authRepository.save(auth);
    }

    //소셜 가입한 유저를 조회, 없으면 예외 발생 (GET의 역할을 위해 구현하였는데..)
    //가입 코드와 겹치는 부분이 있지만 로그인에서 사용하는 로직 (겹치는 부분을 private으로 분리하는 것이 좋을까요??)
    public Auth getUserByProviderAndOauthId(String provider, String oauthId) {
        return authRepository.findByProviderAndOauthId(provider, oauthId)
                .orElseThrow(ErrorCode.USER_NOT_FOUND::serviceException);
    }
}