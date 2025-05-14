package me.performancereservation.domain.user.entitiy;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import me.performancereservation.domain.common.BaseEntity;
import me.performancereservation.domain.user.enums.Role;

@Entity
@NoArgsConstructor
@Getter
public class User extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 유저 ID

    private String email; // 유저 Email

    private String name; // 유저 이름

    private String phoneNumber; // 휴대폰번호

    @Enumerated(EnumType.STRING)
    private Role role; // 권한

    @Builder
    public User(Long id, String email, String name, String phoneNumber, Role role) {
        this.id = id;
        this.email = email;
        this.name = name;
        this.phoneNumber = phoneNumber;
        this.role = role;
    }

    public void promoteManager() {
        this.role = Role.MANAGER;
    }
}
