package me.performancereservation.global.config

import io.github.oshai.kotlinlogging.KotlinLogging
import me.performancereservation.domain.admin.Admin
import me.performancereservation.domain.admin.repository.AdminRepository
import org.springframework.beans.factory.annotation.Value
import org.springframework.boot.CommandLineRunner
import org.springframework.context.annotation.Bean
import org.springframework.data.repository.findByIdOrNull
import org.springframework.security.crypto.password.PasswordEncoder
import org.springframework.stereotype.Component

private val log = KotlinLogging.logger {}

@Component
class AdminInitializer(
    private val adminRepository: AdminRepository,
    private val passwordEncoder: PasswordEncoder,
    @Value("\${app.admin.id}") private val adminId: String,
    @Value("\${app.admin.password}") private val adminPassword: String
) {

    // CommandLineRunner는 스프링이 올라가면 자동으로 실행되어 첫 어드민 계정을 만들어 줍니다.
    @Bean
    fun createAdmin(): CommandLineRunner {
        return CommandLineRunner {
            val existingAdmin = adminRepository.findByIdOrNull(adminId)

            if (existingAdmin == null) {
                // 어드민이 없는 경우는 생성
                adminRepository.save(
                    Admin(
                        id = adminId,
                        password = passwordEncoder.encode(adminPassword)
                    )
                )
                log.info { "Admin 생성 완료" }
            } else {
                // 있으면 비밀번호 갱신
                existingAdmin.changePassword(passwordEncoder.encode(adminPassword))
                adminRepository.save(existingAdmin)
                log.info { "Admin 비밀번호 수정 완료" }
            }
        }
    }
}