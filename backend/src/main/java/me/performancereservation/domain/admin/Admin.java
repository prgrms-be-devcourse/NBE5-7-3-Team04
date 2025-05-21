package me.performancereservation.domain.admin;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.user.enums.Role;

@Entity
@NoArgsConstructor
@Getter
public class Admin {
    @Id
    private String id; // 어드민 ID

    private String password; // 어드민 비밀번호

    private Role role; // 어드민 권한 고정

    @Builder
    public Admin(String id, String password) {
        this.id = id;
        this.password = password;
        this.role = Role.ADMIN;
    }

    public void changePassword(String password) {
        this.password = password;
    }
}
