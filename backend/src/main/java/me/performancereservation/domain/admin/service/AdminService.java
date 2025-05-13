package me.performancereservation.domain.admin.service;

import lombok.RequiredArgsConstructor;
import me.performancereservation.domain.admin.Admin;
import me.performancereservation.domain.admin.AdminUserDetails;
import me.performancereservation.domain.admin.repository.AdminRepository;
import me.performancereservation.global.exception.ErrorCode;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminService implements UserDetailsService {

    private final AdminRepository adminRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Admin admin = adminRepository.findById(username)
                .orElseThrow(() -> ErrorCode.ADMIN_NOT_FOUND.domainException("해당하는 정보의 어드민이 없습니다"));

        return new AdminUserDetails(admin);
    }
}
