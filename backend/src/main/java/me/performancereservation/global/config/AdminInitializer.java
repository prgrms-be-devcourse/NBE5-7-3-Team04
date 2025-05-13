package me.performancereservation.global.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import me.performancereservation.domain.admin.Admin;
import me.performancereservation.domain.admin.AdminRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class AdminInitializer {

    private final AdminRepository adminRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.admin.id}")
    private String adminId;

    @Value("${app.admin.password}")
    private String adminPassword;

    // CommandLineRunner는 스프링이 올라가면 자동으로 실행되어 첫 관리자 계정을 만들어 줍니다.
    @Bean
    public CommandLineRunner createAdmin() {
        return args -> {
            //같은 이름의 관리자가 없는 경우에만 생성
            if (adminRepository.findById(adminId).isEmpty()) {
                Admin admin = Admin.builder()
                        .id(adminId)
                        .password(passwordEncoder.encode(adminPassword))
                        .build();

                adminRepository.save(admin);
                log.info("Admin 생성 완료");
            }
        };
    }
}
