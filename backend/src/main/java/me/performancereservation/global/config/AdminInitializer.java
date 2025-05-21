package me.performancereservation.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.admin.Admin;
import me.performancereservation.domain.admin.repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final AdminRepository adminRepository;

//    @Qualifier("adminPasswordEncoder")
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.id}")
    private String adminId;

    @Value("${app.admin.password}")
    private String adminPassword;

    // CommandLineRunner는 스프링이 올라가면 자동으로 실행되어 첫 어드민 계정을 만들어 줍니다.
    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {
            Optional<Admin> admin = adminRepository.findById(adminId);

            // 어드민이 없는 경우는 생성
            if (admin.isEmpty()) {
                adminRepository.save(Admin.builder()
                        .id(adminId)
                        .password(passwordEncoder.encode(adminPassword))
                        .build()
                );

                log.info("Admin 생성 완료");
            }else{
                // 있으면 비밀번호 생성
                admin.get().changePassword(passwordEncoder.encode(adminPassword));

                log.info("Admin 비밀번호 수정 완료");
            }
        };
    }
}
