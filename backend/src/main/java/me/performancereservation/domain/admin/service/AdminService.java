package me.performancereservation.domain.admin.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AdminService {

    /** 세션을 받아 어드민 권한이 있는지 확인
     *
     */
    public void checkAuthentication() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated() || auth.getName().equals("anonymousUser")) {
            throw ErrorCode.ADMIN_AUTHENTICATION_REQUIRED.domainException("어드민 인증이 필요합니다.");
        }
    }
}