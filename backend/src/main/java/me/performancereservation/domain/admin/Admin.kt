package me.performancereservation.domain.admin

import jakarta.persistence.Entity
import jakarta.persistence.EnumType
import jakarta.persistence.Enumerated
import jakarta.persistence.Id
import me.performancereservation.domain.user.enums.Role

@Entity
class Admin(
    @Id
    val id: String,                 // 어드민 ID

    var password: String,           // 어드민 비밀번호

    @Enumerated(EnumType.STRING)
    val role: Role = Role.ADMIN     // 어드민 권한 고정
) {
    fun changePassword(password: String) {
        this.password = password
    }
}
