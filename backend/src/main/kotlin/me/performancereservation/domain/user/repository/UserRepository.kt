package me.performancereservation.domain.user.repository

import me.performancereservation.domain.user.entitiy.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserRepository : JpaRepository<User, Long> {
    fun findByEmail(email: String?): User? //이메일로 유저 찾기
    fun existsByEmail(email: String?): Boolean //이메일 중복 체크
}
